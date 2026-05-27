package com.nexaworks.rafiq.service.user;

import static com.nexaworks.rafiq.entities.enums.Roles.*;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.request.user.DoctorRegistrationRequest;
import com.nexaworks.rafiq.dto.request.user.UserRegistrationRequest;
import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.entities.*;
import com.nexaworks.rafiq.exception.custom.user.RegistrationException;
import com.nexaworks.rafiq.mapper.UserMapper;
import com.nexaworks.rafiq.rabbit.manager.UserNotificationManager;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.doctor.IDoctorPersistenceService;
import com.nexaworks.rafiq.service.patient.PatientService;
import com.nexaworks.rafiq.utils.AuthSessionManager;
import com.nexaworks.rafiq.utils.TransactionUtils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final TokenService tokenService;
    private final AuthSessionManager authSessionManager;
    private final PatientService patientService;
    private final IDoctorPersistenceService doctorService;
    private final TransactionUtils transactionUtils;
    private final UserNotificationManager manager;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void registerPatient(UserRegistrationRequest request) {
        Patient user = userMapper.toUser(request);
        verifyEmailAvailability(user);
        user.getRoles().add(roleService.getRole(ROLE_PATIENT));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        patientService.register((Patient) user);
        String otp = tokenService.generateOtpToken(user);
        transactionUtils.afterCommit(() -> manager.sendRegistrationEvent(user, otp));
    }

    @Override
    @Transactional
    public void registerDoctor(DoctorRegistrationRequest request, MultipartFile nationalId)
            throws IOException {

        Doctor user = userMapper.toDoctor(request.user());

        verifyEmailAvailability(user);
        user.getRoles().add(roleService.getRole(ROLE_DOCTOR));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        doctorService.register(user, request.specialization(), request.description());

        String otp = tokenService.generateOtpToken(user);
        transactionUtils.afterCommit(() -> manager.sendRegistrationEvent(user, otp));
    }

    @Override
    @Transactional
    public LoginResponse verifyUserEmail(String email, String otp, HttpServletResponse response) {
        User user = tokenService.verifyOtp(email, otp);
        user.setEnabled(true);
        userRepository.save(user);
        return authSessionManager.createLoginSession(response, user);
    }

    @Override
    @Transactional
    public User addUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.getRoles().add(roleService.getRole(ROLE_USER));
        User oAuthUser = userRepository.save(user);
        log.info("User created {}", user.getEmail());
        return oAuthUser;
    }

    public UUID getUserId() {
        return (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    @Transactional
    public Optional<User> getUser(String email, String firstName, String lastName) {
        Optional<User> user = userRepository.findByEmail(email);
        user.ifPresent(value -> value.setEnabled(true));
        if (user.isEmpty()) {
            user = Optional.ofNullable(addUser(email, firstName, lastName));
        }
        return user;
    }
    private void verifyEmailAvailability(User user) {
        if (userRepository.existsUserByEmail(user.getEmail())) {
            throw new RegistrationException(
                    "User with email " + user.getEmail() + " already exists");
        }
    }

}
