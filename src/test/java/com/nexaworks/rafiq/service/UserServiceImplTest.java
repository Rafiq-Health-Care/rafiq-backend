package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.request.ResetPasswordRequest;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.InvalidPasswordException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

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

}
