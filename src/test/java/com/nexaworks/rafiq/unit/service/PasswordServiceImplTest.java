package com.nexaworks.rafiq.unit.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.dto.request.user.ChangePasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ForgetPasswordRequest;
import com.nexaworks.rafiq.dto.request.user.ResetPasswordRequest;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.user.TokenInvalidException;
import com.nexaworks.rafiq.rabbit.manager.UserNotificationManager;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;
import com.nexaworks.rafiq.service.user.PasswordServiceImpl;
import com.nexaworks.rafiq.service.user.TokenService;
import com.nexaworks.rafiq.utils.TransactionUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordServiceImpl Unit Tests")
class PasswordServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserNotificationManager messageService;

    @Mock
    private AuthService authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TransactionUtils transactionUtils;

    @InjectMocks
    private PasswordServiceImpl passwordService;

    private User testUser;
    private Token testToken;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEnabled(true);

        testToken = new Token();
        testToken.setToken("test-token");
        testToken.setUser(testUser);
        testToken.setExpiryDate(LocalDateTime.now().plusHours(1));
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    private void triggerAfterCommitCallbacks() {
        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> {
            try {
                sync.afterCommit();
            } catch (Exception ignored) {
            }
        });
    }

    @Nested
    @DisplayName("Forget Password Tests")
    class ForgetPasswordTests {

        @Test
        @DisplayName("Should generate access token and send reset message when user exists")
        void shouldGenerateAccessTokenAndPublishEventWhenUserExists() {
            // Arrange
            ForgetPasswordRequest request = new ForgetPasswordRequest("test@example.com");
            String generatedToken = "123456";

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(testUser));
            when(tokenService.generateAccessToken(Optional.of(testUser)))
                    .thenReturn(generatedToken);
            doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(0);
                runnable.run();
                return null;
            }).when(transactionUtils).afterCommit(any(Runnable.class));

            // Act
            passwordService.forgetPassword(request);
            triggerAfterCommitCallbacks();

            // Assert
            verify(userRepository).findByEmail(request.email());
            verify(tokenService).generateAccessToken(Optional.of(testUser));
            verify(messageService).sendResetPasswordEvent(eq(testUser), eq(generatedToken));
        }

        @Test
        @DisplayName("Should return silently when user does not exist")
        void shouldReturnSilentlyWhenUserDoesNotExist() {
            // Arrange
            ForgetPasswordRequest request = new ForgetPasswordRequest("nonexistent@example.com");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            // Act
            passwordService.forgetPassword(request);

            // Assert
            verify(userRepository).findByEmail(request.email());
            verify(tokenService, never()).generateAccessToken(any());
            verify(messageService, never()).sendResetPasswordEvent(any(User.class), anyString());
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully with valid access token")
        void shouldChangePasswordSuccessfullyWithValidAccessToken() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest("access-token",
                    "newPassword123");
            when(tokenService.getToken(request.accessToken())).thenReturn(testToken);
            when(passwordEncoder.encode(request.newPassword())).thenReturn("encoded-password");

            // Act
            passwordService.changePassword(request);

            // Assert
            verify(tokenService).getToken(request.accessToken());
            verify(passwordEncoder).encode(request.newPassword());
            org.assertj.core.api.Assertions.assertThat(testUser.getPassword())
                    .isEqualTo("encoded-password");
        }

        @Test
        @DisplayName("Should throw exception when access token is expired")
        void shouldThrowExceptionWhenAccessTokenIsExpired() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest("expired-token",
                    "newPassword123");
            testToken.setExpiryDate(LocalDateTime.now().minusHours(1));
            when(tokenService.getToken(request.accessToken())).thenReturn(testToken);

            // Act & Assert
            assertThatThrownBy(() -> passwordService.changePassword(request))
                    .isInstanceOf(TokenInvalidException.class).hasMessage("Invalid Access Token");

            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("Reset Password Tests")
    class ResetPasswordTests {

        @Test
        @DisplayName("Should reset password for authenticated user")
        void shouldResetPasswordForAuthenticatedUser() {
            // Arrange
            ResetPasswordRequest request = new ResetPasswordRequest("oldPass", "newPass123");

            testUser.setPassword("encoded-old");
            when(authService.getAuthenticateUser()).thenReturn(testUser);
            when(passwordEncoder.matches(request.oldPassword(), testUser.getPassword()))
                    .thenReturn(true);
            when(passwordEncoder.encode(request.newPassword())).thenReturn("encoded-new");

            // Act
            passwordService.resetPassword(request);

            // Assert
            verify(authService).getAuthenticateUser();
            verify(passwordEncoder).matches(request.oldPassword(), "encoded-old");
            verify(passwordEncoder).encode(request.newPassword());
            verify(userRepository).save(testUser);
            org.assertj.core.api.Assertions.assertThat(testUser.getPassword())
                    .isEqualTo("encoded-new");
        }

        @Test
        @DisplayName("Should not change password when old password does not match")
        void shouldNotChangePasswordWhenOldPasswordDoesNotMatch() {
            // Arrange
            ResetPasswordRequest request = new ResetPasswordRequest("wrongOld", "newPass123");

            testUser.setPassword("encoded-old");
            when(authService.getAuthenticateUser()).thenReturn(testUser);
            when(passwordEncoder.matches(request.oldPassword(), testUser.getPassword()))
                    .thenReturn(false);

            // Act
            passwordService.resetPassword(request);

            // Assert
            verify(authService).getAuthenticateUser();
            verify(passwordEncoder).matches(request.oldPassword(), "encoded-old");
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository).save(testUser);
            org.assertj.core.api.Assertions.assertThat(testUser.getPassword())
                    .isEqualTo("encoded-old");
        }
    }
}
