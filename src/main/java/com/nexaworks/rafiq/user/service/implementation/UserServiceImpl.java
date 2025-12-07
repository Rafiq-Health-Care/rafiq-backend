package com.nexaworks.rafiq.user.service.implementation;

import static com.nexaworks.rafiq.user.entity.enums.Roles.*;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.shared.event.doctor.DoctorRegisterEvent;
import com.nexaworks.rafiq.shared.event.patient.PatientRegistrationEvent;
import com.nexaworks.rafiq.user.api.dto.response.LoginResponse;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.exception.RegistrationException;
import com.nexaworks.rafiq.user.repository.UserRepository;
import com.nexaworks.rafiq.user.service.RoleService;
import com.nexaworks.rafiq.user.service.TokenService;
import com.nexaworks.rafiq.user.service.UserService;
import com.nexaworks.rafiq.user.utils.AuthSessionManager;

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
    private final ApplicationEventPublisher eventPublisher;
    private final AuthSessionManager authSessionManager;

    @Override
    @Transactional
    public void registerPatient(User user) {
        verifyEmailAvailability(user);
        user.getRoles().add(roleService.getRole(ROLE_PATIENT));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String otp = tokenService.generateOtpToken(user);
        UUID userId = userRepository.save(user).getId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("OTP sent to {}", user.getEmail());
                eventPublisher.publishEvent(new PatientRegistrationEvent(user.getEmail(), otp,
                        user.getFirstName(), user.getLastName(), userId));
            }
        });
    }

    @Override
    @Transactional
    public void registerDoctor(User user, MultipartFile nationalId, UUID specialization,
            String description) throws IOException {

        verifyEmailAvailability(user);
        user.getRoles().add(roleService.getRole(ROLE_DOCTOR));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        String otp = tokenService.generateOtpToken(user);
        UUID userId = userRepository.save(user).getId();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new DoctorRegisterEvent(
                        new PatientRegistrationEvent(user.getEmail(), otp, user.getFirstName(),
                                user.getLastName(), userId),
                        user.getId(), nationalId, specialization));
            }
        });
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
