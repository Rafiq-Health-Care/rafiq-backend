package com.nexaworks.rafiq.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.dto.UploadResults;
import com.nexaworks.rafiq.dto.event.NewOtpEvent;
import com.nexaworks.rafiq.dto.event.UserRegistrationEvent;
import com.nexaworks.rafiq.dto.request.user.ResetPasswordRequest;
import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.entities.DoctorProfile;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.Roles;
import com.nexaworks.rafiq.exception.custom.InvalidPasswordException;
import com.nexaworks.rafiq.exception.custom.RegistrationException;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.exception.custom.TokenNotFoundException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.ImageService;
import com.nexaworks.rafiq.service.ServiceImpl.*;
import com.nexaworks.rafiq.utils.AuthSessionManager;

import jakarta.servlet.http.HttpServletResponse;

public class UserServiceImplTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RoleServiceImpl roleService;

    @Mock
    TokenServiceImpl tokenService;

    @Mock
    ImageService imageService;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    AuthSessionManager authSessionManager;

    @Mock
    DoctorServiceImpl doctorService;

    @Mock
    PatientServiceImpl patientService;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TransactionSynchronizationManager.initSynchronization();

    }
    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    /**
     * Helper method to manually trigger transaction synchronization callbacks. In
     * unit tests, there's no real transaction commit, so we need to manually invoke
     * the afterCommit callbacks that were registered.
     */
    private void triggerTransactionSynchronization() {
        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> {
            try {
                sync.afterCommit();
            } catch (Exception e) {
                // Ignore exceptions in test
            }
        });
    }

    @DisplayName("Update password should encode and save the new password")
    @Test
    void updatePassword_ShouldEncodeAndSaveNewPassword_WhenOldPasswordMatches() {
        User user = User.builder().id(java.util.UUID.randomUUID()).build();
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest("123", "123");
        when(passwordEncoder.matches(any(), any())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("123");
        when(userRepository.save(user)).thenReturn(user);
        userService.updatePassword(user, resetPasswordRequest);
        verify(userRepository, times(1)).save(user);
    }

    @DisplayName("Update password should throw exception if old password doesn't match")
    @Test
    void updatePassword_ShouldThrowException_WhenOldPasswordDoesNotMatch() {
        User user = User.builder().id(java.util.UUID.randomUUID()).build();
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest("123", "123");
        when(passwordEncoder.matches(any(), any())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("123");
        assertThrows(InvalidPasswordException.class,
                () -> userService.updatePassword(user, resetPasswordRequest));
        verify(userRepository, never()).save(user);
    }

    @DisplayName("Register patient should add user and publish event to send the activation email")
    @Test
    void registerPatient_ShouldAddUserAndPublishEventToSendActivationEmail_WhenPatientIsRegistered() {
        User user = getUser();

        PatientProfile expectedProfile = new PatientProfile();
        String expectedToken = "123456";
        Role patientRole = new Role();
        patientRole.setName("PATIENT");

        when(roleService.getRole(any())).thenReturn(patientRole);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(patientService.createPatientProfile(any(User.class))).thenReturn(expectedProfile);
        when(tokenService.generateOtpToken(any(User.class))).thenReturn(expectedToken);

        userService.registerPatient(user);
        triggerTransactionSynchronization();

        verify(roleService, times(1)).getRole(Roles.ROLE_USER);
        verify(roleService, times(1)).getRole(Roles.ROLE_PATIENT);
        verify(userRepository, times(1)).save(any(User.class));
        verify(patientService, times(1)).createPatientProfile(any(User.class));
        verify(tokenService, times(1)).generateOtpToken(any(User.class));
        verify(eventPublisher, times(1)).publishEvent(any(UserRegistrationEvent.class));
    }

    private static User getUser() {

        return User.builder().id(UUID.randomUUID()).email("john.doe@example.com").firstName("John")
                .lastName("Doe").password("encodedPassword123").tokens(new java.util.ArrayList<>())
                .build();
    }

    @DisplayName("Register patient should throw exception when user with email already exists")
    @Test
    void registerPatient_ShouldThrowException_WhenUserWithEmailAlreadyExists() {
        User user = getUser();
        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(user));
        assertThrows(com.nexaworks.rafiq.exception.custom.RegistrationException.class,
                () -> userService.registerPatient(user));
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(UserRegistrationEvent.class));
    }

    @DisplayName("Register doctor should add user and publish event to send the activation email")
    @Test
    void registerDoctor_ShouldAddUserAndPublishEventToSendActivationEmail_WhenDoctorIsRegistered()
            throws IOException {
        User user = getUser();
        Role doctorRole = new Role();
        doctorRole.setName("DOCTOR");
        DoctorProfile expectedProfile = new DoctorProfile();
        String expectedToken = "123456";

        when(imageService.uploadResource(any(), any()))
                .thenReturn(new UploadResults("url", "publicId"));
        when(roleService.getRole(any())).thenReturn(doctorRole);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenService.generateOtpToken(any(User.class))).thenReturn(expectedToken);
        when(doctorService.createProfile(any(), anyString(), any())).thenReturn(expectedProfile);

        userService
                .registerDoctor(user,
                        new MockMultipartFile("nationalId", "nationalId.png", "image/png",
                                "dummy image content".getBytes()),
                        UUID.randomUUID(), "Experienced doctor");
        triggerTransactionSynchronization();

        verify(roleService, times(1)).getRole(Roles.ROLE_USER);
        verify(roleService, times(1)).getRole(Roles.ROLE_DOCTOR);
        verify(userRepository, times(1)).save(any(User.class));
        verify(tokenService, times(1)).generateOtpToken(any(User.class));
        verify(eventPublisher, times(1))
                .publishEvent(any(com.nexaworks.rafiq.dto.event.DoctorRegisterEvent.class));
        verify(doctorService, times(1)).createProfile(any(User.class), anyString(), any());
    }

    @DisplayName("Register doctor should throw exception when user with email already exists")
    @Test
    void registerDoctor_ShouldThrowException_WhenUserWithEmailAlreadyExists() {
        User user = getUser();
        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(user));
        assertThrows(RegistrationException.class,
                () -> userService.registerDoctor(user, null, null, null));
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(UserRegistrationEvent.class));
    }

    @DisplayName("Verify user email should create login tokens")
    @Test
    void verifyUserEmail_ShouldCreateLoginTokens_WhenOtpIsValid() {
        User user = getUser();

        when(tokenService.verifyOtp(anyString(), anyString())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authSessionManager.createLoginSession(any(HttpServletResponse.class), eq(user)))
                .thenReturn(new LoginResponse(Optional.of("ROLE_USER")));

        LoginResponse response = userService.verifyUserEmail("john.doe@example.com", "123456",
                new MockHttpServletResponse());

        assertEquals(Optional.of("ROLE_USER"), response.role());
        verify(tokenService, times(1)).verifyOtp(anyString(), anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(authSessionManager, times(1)).createLoginSession(any(), any());
    }

    @DisplayName("Verify user email should throw exception when otp is invalid")
    @Test
    void verifyUserEmail_ShouldThrowException_WhenOtpIsInvalid() {
        when(tokenService.verifyOtp(anyString(), anyString())).thenThrow(TokenNotFoundException.class);
        assertThrows(
                TokenNotFoundException.class,
                () -> userService.verifyUserEmail("john@gmail.com", "1234", new MockHttpServletResponse()));
        verify(tokenService, times(1)).verifyOtp(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(authSessionManager, never()).createLoginSession(any(), any());
    }

    @DisplayName("Verify user email should throw exception when otp is expired")
    @Test
    void verifyUserEmail_ShouldThrowException_WhenOtpIsExpired() {
        when(tokenService.verifyOtp(anyString(), anyString())).thenThrow(TokenInvalidException.class);
        assertThrows(
                TokenInvalidException.class,
                () -> userService.verifyUserEmail("john@gmail.com", "1234", new MockHttpServletResponse()));
        verify(tokenService, times(1)).verifyOtp(anyString(), anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(authSessionManager, never()).createLoginSession(any(), any());
    }

    @DisplayName("Get new otp should throw event if the user is existed")
    @Test
    void getNewOtp_ShouldThrowNewOtpEvent_WhenUserIsExisted() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(getUser()));
        when(tokenService.generateOtpToken(any(User.class))).thenReturn("123456");

        userService.getNewOtp("test@email.com");
        triggerTransactionSynchronization();

        verify(eventPublisher, times(1)).publishEvent(any(NewOtpEvent.class));
    }
    @DisplayName("Get new otp should not throw event if the user is not existed")
    @Test
    void getNewOtp_ShouldNotThrowNewOtpEvent_WhenUserIsNotExisted() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        userService.getNewOtp("test@gmail.com");
        verify(eventPublisher, never()).publishEvent(any(NewOtpEvent.class));
    }
}
