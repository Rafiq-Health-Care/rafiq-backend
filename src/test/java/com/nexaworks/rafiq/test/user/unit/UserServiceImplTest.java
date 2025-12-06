package com.nexaworks.rafiq.test.user.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.doctor.entity.model.Doctor;
import com.nexaworks.rafiq.doctor.entity.model.Specialization;
import com.nexaworks.rafiq.doctor.service.implementation.DoctorServiceImpl;
import com.nexaworks.rafiq.doctor.service.implementation.SpecializationServiceImpl;
import com.nexaworks.rafiq.fileManagment.service.FileService;
import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.patient.service.implementation.PatientServiceImpl;
import com.nexaworks.rafiq.shared.event.doctor.DoctorRegisterEvent;
import com.nexaworks.rafiq.shared.event.patient.PatientRegistrationEvent;
import com.nexaworks.rafiq.user.exception.RegistrationException;
import com.nexaworks.rafiq.user.api.dto.response.LoginResponse;
import com.nexaworks.rafiq.user.entity.enums.Roles;
import com.nexaworks.rafiq.user.entity.model.Role;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.exception.TokenInvalidException;
import com.nexaworks.rafiq.user.exception.TokenNotFoundException;
import com.nexaworks.rafiq.user.repository.UserRepository;
import com.nexaworks.rafiq.user.service.implementation.RoleServiceImpl;
import com.nexaworks.rafiq.user.service.implementation.TokenServiceImpl;
import com.nexaworks.rafiq.user.service.implementation.UserServiceImpl;
import com.nexaworks.rafiq.user.utils.AuthSessionManager;

import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleServiceImpl roleService;

    @Mock
    private TokenServiceImpl tokenService;

    @Mock
    private FileService fileService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuthSessionManager authSessionManager;

    @Mock
    private DoctorServiceImpl doctorService;

    @Mock
    private PatientServiceImpl patientService;

    @Mock
    private SpecializationServiceImpl specializationService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
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

    @DisplayName("Register patient should add user and publish event to send the activation email")
    @Test
    void registerPatient_ShouldAddUserAndPublishEventToSendActivationEmail_WhenPatientIsRegistered() {
        // Arrange
        Patient patient = Patient.builder()
                .id(UUID.randomUUID())
                .email("patient@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .password("password123")
                .build();

        String expectedToken = "123456";
        Role patientRole = new Role();
        patientRole.setName("PATIENT");

        when(userRepository.existsUserByEmail(anyString())).thenReturn(false);
        when(roleService.getRole(Roles.ROLE_PATIENT)).thenReturn(patientRole);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(Patient.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.generateOtpToken(any(Patient.class))).thenReturn(expectedToken);

        // Act
        userService.registerPatient(patient);
        triggerTransactionSynchronization();

        // Assert
        verify(userRepository, times(1)).existsUserByEmail(patient.getEmail());
        verify(roleService, times(1)).getRole(Roles.ROLE_PATIENT);
        verify(passwordEncoder, times(1)).encode(patient.getPassword());
        verify(userRepository, times(1)).save(any(Patient.class));
        verify(tokenService, times(1)).generateOtpToken(any(Patient.class));
        verify(eventPublisher, times(1)).publishEvent(any(PatientRegistrationEvent.class));
    }

    @DisplayName("Register patient should throw exception when user with email already exists")
    @Test
    void registerPatient_ShouldThrowException_WhenUserWithEmailAlreadyExists() {
        // Arrange
        Patient patient = Patient.builder()
                .email("existing@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .password("password123")
                .build();

        when(userRepository.existsUserByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RegistrationException.class, () -> userService.registerPatient(patient));
        verify(userRepository, times(1)).existsUserByEmail(patient.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(PatientRegistrationEvent.class));
    }

    @DisplayName("Register doctor should add user and publish event to send the activation email")
    @Test
    void registerDoctor_ShouldAddUserAndPublishEventToSendActivationEmail_WhenDoctorIsRegistered()
            throws IOException {
        // Arrange
        Doctor doctor = Doctor.builder()
                .id(UUID.randomUUID())
                .email("doctor@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .build();

        Role doctorRole = new Role();
        doctorRole.setName("DOCTOR");

        Specialization specialization = Specialization.builder()
                .id(UUID.randomUUID())
                .name("Cardiology")
                .build();

        UUID specializationId = specialization.getId();
        String expectedToken = "123456";
        String description = "Experienced doctor";
        MockMultipartFile nationalId = new MockMultipartFile("nationalId", "nationalId.png",
                "image/png", "dummy image content".getBytes());

        when(userRepository.existsUserByEmail(anyString())).thenReturn(false);
        when(roleService.getRole(Roles.ROLE_DOCTOR)).thenReturn(doctorRole);
        when(specializationService.getSpecialization(specializationId)).thenReturn(specialization);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(Doctor.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenService.generateOtpToken(any(Doctor.class))).thenReturn(expectedToken);

        // Act
        userService.registerDoctor(doctor, nationalId, specializationId, description);
        triggerTransactionSynchronization();

        // Assert
        verify(userRepository, times(1)).existsUserByEmail(doctor.getEmail());
        verify(roleService, times(1)).getRole(Roles.ROLE_DOCTOR);
        verify(specializationService, times(1)).getSpecialization(specializationId);
        verify(passwordEncoder, times(1)).encode(doctor.getPassword());
        verify(userRepository, times(1)).save(any(Doctor.class));
        verify(tokenService, times(1)).generateOtpToken(any(Doctor.class));
        verify(eventPublisher, times(1)).publishEvent(any(DoctorRegisterEvent.class));

        // Verify that Doctor object has the correct properties set
        verify(userRepository).save(argThat(savedDoctor -> savedDoctor instanceof Doctor
                && ((Doctor) savedDoctor).getDescription().equals(description)
                && ((Doctor) savedDoctor).getSpecialization().equals(specialization)));
    }

    @DisplayName("Register doctor should throw exception when user with email already exists")
    @Test
    void registerDoctor_ShouldThrowException_WhenUserWithEmailAlreadyExists() {
        // Arrange
        Doctor doctor = Doctor.builder()
                .email("existing@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("password123")
                .build();

        when(userRepository.existsUserByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(RegistrationException.class,
                () -> userService.registerDoctor(doctor, null, null, null));
        verify(userRepository, times(1)).existsUserByEmail(doctor.getEmail());
        verify(userRepository, never()).save(any(User.class));
        verify(eventPublisher, never()).publishEvent(any(DoctorRegisterEvent.class));
    }

    @DisplayName("Verify user email should create login tokens")
    @Test
    void verifyUserEmail_ShouldCreateLoginTokens_WhenOtpIsValid() {
        // Arrange
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword123")
                .tokens(new java.util.ArrayList<>())
                .build();

        LoginResponse expectedResponse = new LoginResponse(Optional.of("ROLE_USER"));

        when(tokenService.verifyOtp(anyString(), anyString())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(authSessionManager.createLoginSession(any(HttpServletResponse.class), eq(user)))
                .thenReturn(expectedResponse);

        // Act
        LoginResponse response = userService.verifyUserEmail("john.doe@example.com", "123456",
                new MockHttpServletResponse());

        // Assert
        assertEquals(Optional.of("ROLE_USER"), response.role());
        verify(tokenService, times(1)).verifyOtp("john.doe@example.com", "123456");
        verify(userRepository, times(1)).save(user);
        verify(authSessionManager, times(1)).createLoginSession(any(), eq(user));
    }

    @DisplayName("Verify user email should throw exception when otp is invalid")
    @Test
    void verifyUserEmail_ShouldThrowException_WhenOtpIsInvalid() {
        // Arrange
        when(tokenService.verifyOtp(anyString(), anyString()))
                .thenThrow(new TokenNotFoundException("Token not found"));

        // Act & Assert
        assertThrows(TokenNotFoundException.class,
                () -> userService.verifyUserEmail("john@gmail.com", "1234", new MockHttpServletResponse()));
        verify(tokenService, times(1)).verifyOtp("john@gmail.com", "1234");
        verify(userRepository, never()).save(any(User.class));
        verify(authSessionManager, never()).createLoginSession(any(), any());
    }

    @DisplayName("Verify user email should throw exception when otp is expired")
    @Test
    void verifyUserEmail_ShouldThrowException_WhenOtpIsExpired() {
        // Arrange
        when(tokenService.verifyOtp(anyString(), anyString()))
                .thenThrow(new TokenInvalidException("Token expired"));

        // Act & Assert
        assertThrows(TokenInvalidException.class,
                () -> userService.verifyUserEmail("john@gmail.com", "1234", new MockHttpServletResponse()));
        verify(tokenService, times(1)).verifyOtp("john@gmail.com", "1234");
        verify(userRepository, never()).save(any(User.class));
        verify(authSessionManager, never()).createLoginSession(any(), any());
    }
}

