package com.nexaworks.rafiq.unit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.dto.request.user.DoctorRegistrationRequest;
import com.nexaworks.rafiq.dto.request.user.UserRegistrationRequest;
import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.Roles;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.exception.custom.user.RegistrationException;
import com.nexaworks.rafiq.exception.custom.user.TokenInvalidException;
import com.nexaworks.rafiq.exception.custom.user.TokenNotFoundException;
import com.nexaworks.rafiq.mapper.UserMapper;
import com.nexaworks.rafiq.rabbit.manager.UserNotificationManager;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.doctor.DoctorPersistenceService;
import com.nexaworks.rafiq.service.patient.PatientServiceImpl;
import com.nexaworks.rafiq.service.user.RoleServiceImpl;
import com.nexaworks.rafiq.service.user.TokenServiceImpl;
import com.nexaworks.rafiq.service.user.UserServiceImpl;
import com.nexaworks.rafiq.utils.AuthSessionManager;
import com.nexaworks.rafiq.utils.TransactionUtils;

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
    UserNotificationManager messageService;

    @Mock
    AuthSessionManager authSessionManager;

    @Mock
    DoctorPersistenceService doctorService;

    @Mock
    PatientServiceImpl patientService;

    @Mock
    TransactionUtils transactionUtils;

    @Mock
    UserMapper userMapper;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        TransactionSynchronizationManager.initSynchronization();
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(transactionUtils).afterCommit(any(Runnable.class));

    }
    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    private void triggerTransactionSynchronization() {
        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> {
            try {
                sync.afterCommit();
            } catch (Exception ignored) {
            }
        });
    }

    @DisplayName("Register patient should add user and publish event to send the activation email")
    @Test
    void registerPatient_ShouldAddUserAndPublishEventToSendActivationEmail_WhenPatientIsRegistered() {
        UserRegistrationRequest request = new UserRegistrationRequest("patient@example.com",
                "Password@123", "Jane", "Doe", "+12345678901", "female", LocalDate.of(1995, 1, 1));

        // Create Patient object (Patient extends User with is-a relationship)
        Patient patient = Patient.builder().id(UUID.randomUUID()).email("patient@example.com")
                .firstName("Jane").lastName("Doe").password("password123").roles(new HashSet<>())
                .build();

        String expectedToken = "123456";
        Role patientRole = new Role();
        patientRole.setName("PATIENT");

        when(userRepository.existsUserByEmail(anyString())).thenReturn(false);
        when(roleService.getRole(Roles.ROLE_PATIENT)).thenReturn(patientRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.toUser(request)).thenReturn(patient);
        when(tokenService.generateOtpToken(any(Patient.class))).thenReturn(expectedToken);

        userService.registerPatient(request);
        triggerTransactionSynchronization();

        verify(roleService, times(1)).getRole(Roles.ROLE_PATIENT);
        verify(patientService, times(1)).register(any(Patient.class));
        verify(tokenService, times(1)).generateOtpToken(any(Patient.class));
        verify(messageService, times(1)).sendRegistrationEvent(eq(patient), eq(expectedToken));
    }

    private static User getUser() {

        return User.builder().id(UUID.randomUUID()).email("john.doe@example.com").firstName("John")
                .lastName("Doe").password("encodedPassword123").tokens(new java.util.ArrayList<>())
                .build();
    }

    @DisplayName("Register patient should throw exception when user with email already exists")
    @Test
    void registerPatient_ShouldThrowException_WhenUserWithEmailAlreadyExists() {
        UserRegistrationRequest request = new UserRegistrationRequest("existing@example.com",
                "Password@123", "Jane", "Doe", "+12345678901", "female", LocalDate.of(1995, 1, 1));

        Patient patient = Patient.builder().email("existing@example.com").firstName("Jane")
                .lastName("Doe").password("password123").build();

        when(userRepository.existsUserByEmail(anyString())).thenReturn(true);
        when(userMapper.toUser(request)).thenReturn(patient);
        assertThrows(RegistrationException.class, () -> userService.registerPatient(request));
        verify(userRepository, never()).save(any(User.class));
        verify(messageService, never()).sendRegistrationEvent(any(User.class), anyString());
    }

    @DisplayName("Register doctor should add user and publish event to send the activation email")
    @Test
    void registerDoctor_ShouldAddUserAndPublishEventToSendActivationEmail_WhenDoctorIsRegistered()
            throws IOException {
        UserRegistrationRequest userRequest = new UserRegistrationRequest("doctor@example.com",
                "Password@123", "John", "Doe", "+12345678901", "male", LocalDate.of(1990, 1, 1));
        DoctorRegistrationRequest request = new DoctorRegistrationRequest(userRequest,
                Specialization.CARDIOLOGY, "Experienced doctor");

        Doctor doctor = Doctor.builder().id(UUID.randomUUID()).email("doctor@example.com")
                .firstName("John").lastName("Doe").password("password123").roles(new HashSet<>())
                .build();

        Role doctorRole = new Role();
        doctorRole.setName("DOCTOR");

        Specialization specialization = Specialization.CARDIOLOGY;
        String expectedToken = "123456";
        String description = "Experienced doctor";

        when(userRepository.existsUserByEmail(anyString())).thenReturn(false);
        when(roleService.getRole(Roles.ROLE_DOCTOR)).thenReturn(doctorRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userMapper.toDoctor(userRequest)).thenReturn(doctor);
        when(tokenService.generateOtpToken(any(Doctor.class))).thenReturn(expectedToken);

        userService.registerDoctor(request, new MockMultipartFile("nationalId", "nationalId.png",
                "image/png", "dummy image content".getBytes()));
        triggerTransactionSynchronization();

        verify(roleService, times(1)).getRole(Roles.ROLE_DOCTOR);
        verify(doctorService, times(1)).register(any(Doctor.class), eq(specialization),
                eq(description));
        verify(tokenService, times(1)).generateOtpToken(any(Doctor.class));
        verify(messageService, times(1)).sendRegistrationEvent(eq(doctor), eq(expectedToken));
    }

    @DisplayName("Register doctor should throw exception when user with email already exists")
    @Test
    void registerDoctor_ShouldThrowException_WhenUserWithEmailAlreadyExists() {
        UserRegistrationRequest userRequest = new UserRegistrationRequest("existing@example.com",
                "Password@123", "John", "Doe", "+12345678901", "male", LocalDate.of(1990, 1, 1));
        DoctorRegistrationRequest request = new DoctorRegistrationRequest(userRequest,
                Specialization.CARDIOLOGY, "Experienced doctor");

        Doctor doctor = Doctor.builder().email("existing@example.com").firstName("John")
                .lastName("Doe").password("password123").build();

        when(userRepository.existsUserByEmail(anyString())).thenReturn(true);
        when(userMapper.toDoctor(userRequest)).thenReturn(doctor);
        assertThrows(RegistrationException.class, () -> userService.registerDoctor(request, null));
        verify(userRepository, never()).save(any(User.class));
        verify(messageService, never()).sendRegistrationEvent(any(User.class), anyString());
    }

    @DisplayName("Verify user email should create login tokens")
    @Test
    void verifyUserEmail_ShouldCreateLoginTokens_WhenOtpIsValid() {
        User user = getUser();

        when(tokenService.verifyOtp(anyString(), anyString())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authSessionManager.createLoginSession(any(HttpServletResponse.class), eq(user)))
                .thenReturn(new LoginResponse("ROLE_USER"));

        LoginResponse response = userService.verifyUserEmail("john.doe@example.com", "123456",
                new MockHttpServletResponse());

        assertEquals("ROLE_USER", response.role());
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

}
