package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.UploadResults;
import com.nexaworks.rafiq.dto.event.UserRegistrationEvent;
import com.nexaworks.rafiq.dto.request.DoctorRegistrationRequest;
import com.nexaworks.rafiq.dto.request.ResetPasswordRequest;
import com.nexaworks.rafiq.dto.request.UserRegistrationRequest;
import com.nexaworks.rafiq.entities.DoctorProfile;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Role;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.Roles;
import com.nexaworks.rafiq.exception.custom.InvalidPasswordException;
import com.nexaworks.rafiq.exception.custom.RegistrationException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.ServiceImpl.*;
import com.nexaworks.rafiq.utils.AuthSessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

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
    }
    @DisplayName("Update password should encode and save the new password")
    @Test
    void updatePassword_ShouldEncodeAndSaveNewPassword_WhenOldPasswordMatches() {
        User user = User.builder().id(java.util.UUID.randomUUID()).build();
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest("123","123");
        when(passwordEncoder.matches(any(),any())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("123");
        when(userRepository.save(user)).thenReturn(user);
        userService.updatePassword(user,resetPasswordRequest);
        verify(userRepository,times(1)).save(user);
    }
    @DisplayName("Update password should throw exception if old password doesn't match")
    @Test
    void updatePassword_ShouldThrowException_WhenOldPasswordDoesNotMatch() {
        User user = User.builder().id(java.util.UUID.randomUUID()).build();
        ResetPasswordRequest resetPasswordRequest = new ResetPasswordRequest("123","123");
        when(passwordEncoder.matches(any(),any())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("123");
        assertThrows(InvalidPasswordException.class,()->userService.updatePassword(user,resetPasswordRequest));
        verify(userRepository,never()).save(user);
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

        verify(roleService, times(1)).getRole(Roles.ROLE_USER);
        verify(roleService, times(1)).getRole(Roles.ROLE_PATIENT);
        verify(userRepository, times(1)).save(any(User.class));
        verify(patientService, times(1)).createPatientProfile(any(User.class));
        verify(tokenService, times(1)).generateOtpToken(any(User.class));
        verify(eventPublisher, times(1)).publishEvent(any(UserRegistrationEvent.class));
    }

    private static User getUser() {

        return User.builder()
                .id(UUID.randomUUID())
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("encodedPassword123")
                .build();
    }

    @DisplayName("Register patient should throw exception when user with email already exists")
    @Test
    void registerPatient_ShouldThrowException_WhenUserWithEmailAlreadyExists() {
        User user = getUser();
        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(user));
        assertThrows(com.nexaworks.rafiq.exception.custom.RegistrationException.class,()->userService.registerPatient(user));
        verify(userRepository,never()).save(any(User.class));
        verify(eventPublisher,never()).publishEvent(any(UserRegistrationEvent.class));

    }

    @DisplayName("Register doctor should add user and publish event to send the activation email")
    @Test
    void registerDoctor_ShouldAddUserAndPublishEventToSendActivationEmail_WhenDoctorIsRegistered() throws IOException {
        User user = getUser();
        Role doctorRole = new Role();
        doctorRole.setName("DOCTOR");
        DoctorProfile expectedProfile = new DoctorProfile();
        String expectedToken = "123456";

        when(imageService.uploadResource(any(),any())).thenReturn(new UploadResults("url","publicId"));
        when(roleService.getRole(any())).thenReturn(doctorRole);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenService.generateOtpToken(any(User.class))).thenReturn(expectedToken);
        when(doctorService.createProfile(any(),anyString(),any(),anyString(),anyString())).thenReturn(expectedProfile);

        userService.registerDoctor(user,
                new MockMultipartFile("nationalId","nationalId.png","image/png","dummy image content".getBytes()),
                UUID.randomUUID(),
                "Experienced doctor");

        verify(roleService, times(1)).getRole(Roles.ROLE_USER);
        verify(roleService, times(1)).getRole(Roles.ROLE_DOCTOR);
        verify(userRepository, times(1)).save(any(User.class));
        verify(tokenService, times(1)).generateOtpToken(any(User.class));
        verify(eventPublisher, times(1)).publishEvent(any(UserRegistrationEvent.class));
        verify(doctorService, times(1)).createProfile(any(User.class),anyString(),any(),anyString(),anyString());
        verify(imageService, times(1)).uploadResource(any(),any());

    }
    @DisplayName("Register doctor should throw exception when user with email already exists")
    @Test
    void registerDoctor_ShouldThrowException_WhenUserWithEmailAlreadyExists() {
        User user = getUser();
        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(user));
        assertThrows(RegistrationException.class,()->userService.registerDoctor(user,null,null,null));
        verify(userRepository,never()).save(any(User.class));
        verify(eventPublisher,never()).publishEvent(any(UserRegistrationEvent.class));
    }

}
