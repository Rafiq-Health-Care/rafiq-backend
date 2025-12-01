package com.nexaworks.rafiq.service.ServiceImpl;

import static com.nexaworks.rafiq.entities.enums.Roles.*;

import java.io.IOException;
import java.time.Instant;
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

import com.nexaworks.rafiq.dto.event.DoctorRegisterEvent;
import com.nexaworks.rafiq.dto.event.NewOtpEvent;
import com.nexaworks.rafiq.dto.event.UserRegistrationEvent;
import com.nexaworks.rafiq.dto.request.user.ResetPasswordRequest;
import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.TokenType;
import com.nexaworks.rafiq.exception.custom.InvalidPasswordException;
import com.nexaworks.rafiq.exception.custom.RegistrationException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.*;
import com.nexaworks.rafiq.utils.AuthSessionManager;

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
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final TokenService tokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuthSessionManager authSessionManager;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional
    public void changePassword(User user, String s) {
        user.setPassword(passwordEncoder.encode(s));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void updatePassword(User user, ResetPasswordRequest resetPasswordRequest) {
        if (!passwordEncoder.matches(resetPasswordRequest.oldPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Old password is not correct");
        }
        user.setPassword(passwordEncoder.encode(resetPasswordRequest.newPassword()));
        userRepository.save(user);
        log.info("Password updated for user {}", user.getEmail());
    }

    @Override
    @Transactional
    public void registerPatient(User user) {
        if (userRepository.existsUserByEmail(user.getEmail())) {
            throw new RegistrationException(
                    "User with email " + user.getEmail() + " already exists");
        }
        User patient = extracted(user);
        patient.getRoles().add(roleService.getRole(ROLE_PATIENT));
        userRepository.save(patient);
        PatientProfile patientProfile = patientService.createPatientProfile(patient);
        patient.setPatientProfile(patientProfile);
        log.info("User registered {}", user.getEmail());
        String otp = tokenService.generateOtpToken(patient);
        log.info("OTP generated {}", otp);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("OTP sent to {}", user.getEmail());
                eventPublisher.publishEvent(
                        new UserRegistrationEvent(user.getEmail(), otp, user.getFirstName()));
            }
        });

    }

    private User extracted(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Role role = roleService.getRole(ROLE_USER);
        user.getRoles().add(role);
        return user;
    }

    @Override
    @Transactional
    public void registerDoctor(User user, MultipartFile nationalId, UUID specialization,
            String description) throws IOException {
        if (userRepository.existsUserByEmail(user.getEmail())) {
            throw new RegistrationException(
                    "User with email " + user.getEmail() + " already exists");
        }
        User doctor = extracted(user);
        userRepository.save(doctor);
        doctor.getRoles().add(roleService.getRole(ROLE_DOCTOR));
        doctor.setDoctorProfile(doctorService.createProfile(doctor, description, specialization));
        String otp = tokenService.generateOtpToken(doctor);
        log.info("OTP generated {}", otp);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new DoctorRegisterEvent(
                        new UserRegistrationEvent(user.getEmail(), otp, user.getFirstName()),
                        doctor.getDoctorProfile().getId(), nationalId));
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
    public void getNewOtp(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return;
        }

        Optional<Token> otpToken = user.get().getTokens().stream()
                .filter(token -> token.getTokenType().equals(TokenType.OTP)
                        && token.getExpiryDate().isAfter(Instant.now()))
                .findFirst();
        otpToken.ifPresent(token -> token.setExpiryDate(Instant.now()));
        String otp = tokenService.generateOtpToken(user.get());
        log.info("New OTP generated {}", otp);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("New OTP sent to {}", user.get().getEmail());
                eventPublisher.publishEvent(
                        new NewOtpEvent(user.get().getEmail(), otp, user.get().getFirstName()));

            }
        });
    }

    @Override
    public User getUser() {

        return userRepository.findById(
                (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .orElseThrow();
    }

    @Override
    @Transactional
    public User addUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(false);
        user.getRoles().add(roleService.getRole(ROLE_USER));
        User oAuthUser = userRepository.save(user);
        log.info("User created {}", user.getEmail());
        return oAuthUser;
    }

    @Override
    public String getNotificationToken() {
        User user = getUser();
        return user.getNotificationToken();
    }
}
