package com.nexaworks.rafiq.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.nexaworks.rafiq.dto.event.ForgetPasswordEvent;
import com.nexaworks.rafiq.dto.request.*;
import com.nexaworks.rafiq.dto.response.LoginResponse;
import com.nexaworks.rafiq.dto.response.VerifyOtpResponse;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.GoogleAuthException;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.ServiceImpl.AuthServiceImpl;
import com.nexaworks.rafiq.utils.AuthSessionManager;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AuthSessionManager authSessionManager;

    @Mock
    private GoogleIdTokenVerifier verifier;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthServiceImpl authService;

    @Captor
    private ArgumentCaptor<ForgetPasswordEvent> eventCaptor;

    @Captor
    private ArgumentCaptor<Cookie> cookieCaptor;

    private User testUser;
    private Token testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEnabled(true);

        testToken = new Token();
        testToken.setToken("test-token");
        testToken.setUser(testUser);
        testToken.setExpiryDate(Instant.now().plus(1, ChronoUnit.HOURS));
    }

    @Nested
    @DisplayName("Forget Password Tests")
    class ForgetPasswordTests {

        @Test
        @DisplayName("Should generate OTP and publish event when user exists")
        void shouldGenerateOtpAndPublishEventWhenUserExists() {
            // Arrange
            ForgetPasswordRequest request = new ForgetPasswordRequest("test@example.com");
            String generatedOtp = "123456";

            when(userService.findByEmail(request.email())).thenReturn(Optional.of(testUser));
            when(tokenService.generateOtpToken(testUser)).thenReturn(generatedOtp);

            // Act
            authService.forgetPassword(request);

            // Assert
            verify(userService).findByEmail(request.email());
            verify(tokenService).generateOtpToken(testUser);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            ForgetPasswordEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.email()).isEqualTo(testUser.getEmail());
            assertThat(capturedEvent.otp()).isEqualTo(generatedOtp);
            assertThat(capturedEvent.name()).isEqualTo(testUser.getFirstName());
        }

        @Test
        @DisplayName("Should return silently when user does not exist")
        void shouldReturnSilentlyWhenUserDoesNotExist() {
            // Arrange
            ForgetPasswordRequest request = new ForgetPasswordRequest("nonexistent@example.com");
            when(userService.findByEmail(request.email())).thenReturn(Optional.empty());

            // Act
            authService.forgetPassword(request);

            // Assert
            verify(userService).findByEmail(request.email());
            verify(tokenService, never()).generateOtpToken(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Verify OTP Tests")
    class VerifyOtpTests {

        @Test
        @DisplayName("Should verify OTP successfully and return access token")
        void shouldVerifyOtpSuccessfullyAndReturnAccessToken() {
            // Arrange
            VerifyOtpRequest request = new VerifyOtpRequest("test@example.com","123456" );
            String accessToken = "access-token-123";

            when(tokenService.getToken(request.otp())).thenReturn(testToken);
            when(userService.findByEmail(request.email())).thenReturn(Optional.of(testUser));
            when(tokenService.generateAccessToken(Optional.of(testUser))).thenReturn(accessToken);

            // Act
            VerifyOtpResponse result = authService.verifyOtp(request);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.accessToken()).isEqualTo(accessToken);
            verify(tokenService).getToken(request.otp());
            verify(tokenService).generateAccessToken(Optional.of(testUser));
        }

        @Test
        @DisplayName("Should throw exception when OTP email does not match")
        void shouldThrowExceptionWhenOtpEmailDoesNotMatch() {
            // Arrange
            VerifyOtpRequest request = new VerifyOtpRequest( "wrong@example.com","123456");
            when(tokenService.getToken(request.otp())).thenReturn(testToken);

            // Act & Assert
            assertThatThrownBy(() -> authService.verifyOtp(request))
                    .isInstanceOf(TokenInvalidException.class)
                    .hasMessage("Invalid OTP");
        }

        @Test
        @DisplayName("Should throw exception when OTP is expired")
        void shouldThrowExceptionWhenOtpIsExpired() {
            // Arrange
            VerifyOtpRequest request = new VerifyOtpRequest( "test@example.com","123456");
            testToken.setExpiryDate(Instant.now().minus(1, ChronoUnit.HOURS));
            when(tokenService.getToken(request.otp())).thenReturn(testToken);

            // Act & Assert
            assertThatThrownBy(() -> authService.verifyOtp(request))
                    .isInstanceOf(TokenInvalidException.class)
                    .hasMessage("Invalid OTP");
        }
    }

    @Nested
    @DisplayName("Change Password Tests")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change password successfully with valid access token")
        void shouldChangePasswordSuccessfullyWithValidAccessToken() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest("access-token", "newPassword123");
            when(tokenService.getToken(request.accessToken())).thenReturn(testToken);

            // Act
            authService.changePassword(request);

            // Assert
            verify(tokenService).getToken(request.accessToken());
            verify(userService).changePassword(testUser, request.newPassword());
        }

        @Test
        @DisplayName("Should throw exception when access token is expired")
        void shouldThrowExceptionWhenAccessTokenIsExpired() {
            // Arrange
            ChangePasswordRequest request = new ChangePasswordRequest("expired-token", "newPassword123");
            testToken.setExpiryDate(Instant.now().minus(1, ChronoUnit.HOURS));
            when(tokenService.getToken(request.accessToken())).thenReturn(testToken);

            // Act & Assert
            assertThatThrownBy(() -> authService.changePassword(request))
                    .isInstanceOf(TokenInvalidException.class)
                    .hasMessage("Invalid Access Token");

            verify(userService, never()).changePassword(any(), anyString());
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

            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            SecurityContextHolder.setContext(securityContext);

            // Act
            authService.resetPassword(request);

            // Assert
            verify(userService).updatePassword(testUser, request);
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfullyWithValidCredentials() {
            // Arrange
            String email = "test@example.com";
            String password = "password123";
            LoginResponse expectedResponse = new LoginResponse(Optional.of("ROLE_USER"));

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            when(authSessionManager.createLoginSession(response, testUser)).thenReturn(expectedResponse);

            // Act
            LoginResponse result = authService.login(email, password, response);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(authSessionManager).createLoginSession(response, testUser);
        }
    }

    @Nested
    @DisplayName("Refresh Token Tests")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully with valid refresh token")
        void shouldRefreshTokenSuccessfullyWithValidRefreshToken() {
            // Arrange
            String refreshToken = "valid-refresh-token";
            LoginResponse expectedResponse = new LoginResponse(Optional.of("ROLE_USER"));

            when(authSessionManager.getCookie(request, "refreshToken")).thenReturn(refreshToken);
            when(tokenService.getToken(refreshToken)).thenReturn(testToken);
            when(authSessionManager.createLoginSession(response, testUser)).thenReturn(expectedResponse);

            // Act
            LoginResponse result = authService.refresh(response, request);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);
            verify(tokenService).getToken(refreshToken);
            verify(authSessionManager).createLoginSession(response, testUser);
        }

        @Test
        @DisplayName("Should throw exception when refresh token is expired")
        void shouldThrowExceptionWhenRefreshTokenIsExpired() {
            // Arrange
            String refreshToken = "expired-refresh-token";
            testToken.setExpiryDate(Instant.now().minus(1, ChronoUnit.HOURS));

            when(authSessionManager.getCookie(request, "refreshToken")).thenReturn(refreshToken);
            when(tokenService.getToken(refreshToken)).thenReturn(testToken);

            // Act & Assert
            assertThatThrownBy(() -> authService.refresh(response, request))
                    .isInstanceOf(TokenInvalidException.class)
                    .hasMessage("Invalid Refresh Token");
        }
    }


    @Nested
    @DisplayName("OAuth2 Tests")
    class OAuth2Tests {

        @Mock
        private GoogleIdToken googleIdToken;

        @Mock
        private GoogleIdToken.Payload payload;

        @Test
        @DisplayName("Should authenticate existing user via Google OAuth2")
        void shouldAuthenticateExistingUserViaGoogleOAuth2() throws Exception {
            // Arrange
            String idToken = "valid-google-id-token";
            LoginResponse expectedResponse = new LoginResponse(Optional.of("ROLE_USER"));

            when(verifier.verify(idToken)).thenReturn(googleIdToken);
            when(googleIdToken.getPayload()).thenReturn(payload);
            when(payload.getEmail()).thenReturn("test@example.com");
            when(payload.get("given_name")).thenReturn("John");
            when(payload.get("family_name")).thenReturn("Doe");
            when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(authSessionManager.createLoginSession(response, testUser)).thenReturn(expectedResponse);

            // Act
            LoginResponse result = authService.oAuth2(idToken, response);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);
            verify(verifier).verify(idToken);
            verify(authSessionManager).createLoginSession(response, testUser);
        }

        @Test
        @DisplayName("Should create and authenticate new user via Google OAuth2")
        void shouldCreateAndAuthenticateNewUserViaGoogleOAuth2() throws Exception {
            // Arrange
            String idToken = "valid-google-id-token";
            LoginResponse expectedResponse = new LoginResponse(Optional.of("ROLE_USER"));

            when(verifier.verify(idToken)).thenReturn(googleIdToken);
            when(googleIdToken.getPayload()).thenReturn(payload);
            when(payload.getEmail()).thenReturn("newuser@example.com");
            when(payload.get("given_name")).thenReturn("Jane");
            when(payload.get("family_name")).thenReturn("Smith");
            when(userService.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
            when(userService.addUser("newuser@example.com", "Jane", "Smith")).thenReturn(testUser);
            when(authSessionManager.createLoginSession(response, testUser)).thenReturn(expectedResponse);

            // Act
            LoginResponse result = authService.oAuth2(idToken, response);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);
            verify(userService).addUser("newuser@example.com", "Jane", "Smith");
            verify(authSessionManager).createLoginSession(response, testUser);
        }

        @Test
        @DisplayName("Should enable disabled user during Google OAuth2 login")
        void shouldEnableDisabledUserDuringGoogleOAuth2Login() throws Exception {
            // Arrange
            String idToken = "valid-google-id-token";
            testUser.setEnabled(false);
            LoginResponse expectedResponse = new LoginResponse(Optional.of("ROLE_USER"));

            when(verifier.verify(idToken)).thenReturn(googleIdToken);
            when(googleIdToken.getPayload()).thenReturn(payload);
            when(payload.getEmail()).thenReturn("test@example.com");
            when(payload.get("given_name")).thenReturn("John");
            when(payload.get("family_name")).thenReturn("Doe");
            when(userService.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(authSessionManager.createLoginSession(response, testUser)).thenReturn(expectedResponse);

            // Act
            LoginResponse result = authService.oAuth2(idToken, response);

            // Assert
            assertThat(testUser.isEnabled()).isTrue();
            verify(userRepository).save(testUser);
            verify(authSessionManager).createLoginSession(response, testUser);
        }

        @Test
        @DisplayName("Should throw exception when Google ID token is null")
        void shouldThrowExceptionWhenGoogleIdTokenIsNull() throws Exception {
            // Arrange
            String idToken = "invalid-token";
            when(verifier.verify(idToken)).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> authService.oAuth2(idToken, response))
                    .isInstanceOf(GoogleAuthException.class)
                    .hasMessage("Invalid id token");
        }

        @Test
        @DisplayName("Should throw exception when Google token verification fails")
        void shouldThrowExceptionWhenGoogleTokenVerificationFails() throws Exception {
            // Arrange
            String idToken = "invalid-token";
            when(verifier.verify(idToken)).thenThrow(new GeneralSecurityException("Verification failed"));

            // Act & Assert
            assertThatThrownBy(() -> authService.oAuth2(idToken, response))
                    .isInstanceOf(GoogleAuthException.class)
                    .hasMessage("Failed to verify Google ID token");
        }

        @Test
        @DisplayName("Should throw exception when IOException occurs during verification")
        void shouldThrowExceptionWhenIOExceptionOccursDuringVerification() throws Exception {
            // Arrange
            String idToken = "invalid-token";
            when(verifier.verify(idToken)).thenThrow(new IOException("Network error"));

            // Act & Assert
            assertThatThrownBy(() -> authService.oAuth2(idToken, response))
                    .isInstanceOf(GoogleAuthException.class)
                    .hasMessage("Failed to verify Google ID token");
        }

        @Test
        @DisplayName("Should throw exception when user creation fails")
        void shouldThrowExceptionWhenUserCreationFails() throws Exception {
            // Arrange
            String idToken = "valid-google-id-token";

            when(verifier.verify(idToken)).thenReturn(googleIdToken);
            when(googleIdToken.getPayload()).thenReturn(payload);
            when(payload.getEmail()).thenReturn("newuser@example.com");
            when(payload.get("given_name")).thenReturn("Jane");
            when(payload.get("family_name")).thenReturn("Smith");
            when(userService.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
            when(userService.addUser(anyString(), anyString(), anyString())).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> authService.oAuth2(idToken, response))
                    .isInstanceOf(GoogleAuthException.class)
                    .hasMessage("Failed to authenticate user with Google");
        }
    }

    @Nested
    @DisplayName("Get Authenticated User Tests")
    class GetAuthenticatedUserTests {

        @Test
        @DisplayName("Should return authenticated user from security context")
        void shouldReturnAuthenticatedUserFromSecurityContext() {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser);
            SecurityContextHolder.setContext(securityContext);

            // Act
            User result = authService.getAuthenticateUser();

            // Assert
            assertThat(result).isEqualTo(testUser);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }
    }
}