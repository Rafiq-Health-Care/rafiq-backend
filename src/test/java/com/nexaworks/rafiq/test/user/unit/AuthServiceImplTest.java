package com.nexaworks.rafiq.test.user.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nexaworks.rafiq.user.api.dto.response.LoginResponse;
import com.nexaworks.rafiq.user.entity.model.Token;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.exception.TokenInvalidException;
import com.nexaworks.rafiq.user.repository.UserRepository;
import com.nexaworks.rafiq.user.service.JwtService;
import com.nexaworks.rafiq.user.service.TokenService;
import com.nexaworks.rafiq.user.service.implementation.AuthServiceImpl;
import com.nexaworks.rafiq.user.utils.AuthSessionManager;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthSessionManager authSessionManager;

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

    private User testUser;
    private Token testToken;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
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
            when(authSessionManager.createLoginSession(response, testUser))
                    .thenReturn(expectedResponse);

            // Act
            LoginResponse result = authService.login(email, password, response);

            // Assert
            assertThat(result).isEqualTo(expectedResponse);
            verify(authenticationManager)
                    .authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(authSessionManager).createLoginSession(response, testUser);
        }

        @Test
        @DisplayName("Should throw exception when credentials are invalid")
        void shouldThrowExceptionWhenCredentialsAreInvalid() {
            // Arrange
            String email = "test@example.com";
            String password = "wrongPassword";

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            // Act & Assert
            assertThatThrownBy(() -> authService.login(email, password, response))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Invalid credentials");
            verify(authSessionManager, never()).createLoginSession(any(), any());
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
            when(authSessionManager.createLoginSession(response, testUser))
                    .thenReturn(expectedResponse);

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
            verify(authSessionManager, never()).createLoginSession(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when refresh token cookie is missing")
        void shouldThrowExceptionWhenRefreshTokenCookieIsMissing() {
            // Arrange
            when(authSessionManager.getCookie(request, "refreshToken")).thenReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> authService.refresh(response, request))
                    .isInstanceOf(TokenInvalidException.class);
            verify(tokenService, never()).getToken(anyString());
            verify(authSessionManager, never()).createLoginSession(any(), any());
        }
    }

    @Nested
    @DisplayName("Get Authenticated User Tests")
    class GetAuthenticatedUserTests {

        @Test
        @DisplayName("Should return authenticated user id from security context")
        void shouldReturnAuthenticatedUserIdFromSecurityContext() {
            // Arrange
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(testUser.getId());
            when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            SecurityContextHolder.setContext(securityContext);

            // Act
            User result = authService.getAuthenticateUser();

            // Assert
            assertThat(result).isEqualTo(testUser);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            verify(userRepository).findById(testUser.getId());
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Arrange
            UUID userId = UUID.randomUUID();
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.empty());
            SecurityContextHolder.setContext(securityContext);

            // Act & Assert
            assertThatThrownBy(() -> authService.getAuthenticateUser())
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Logout Tests")
    class LogoutTests {

        @Test
        @DisplayName("Should logout successfully and clear tokens")
        void shouldLogoutSuccessfullyAndClearTokens() {
            // Arrange
            String refreshToken = "valid-refresh-token";
            when(authSessionManager.getCookie(request, "refreshToken")).thenReturn(refreshToken);
            when(tokenService.getToken(refreshToken)).thenReturn(testToken);
            doNothing().when(tokenService).invalidateRefreshToken(testToken);
            doNothing().when(authSessionManager).invalidateSession(response);

            // Act
            authService.logout(request, response);

            // Assert
            verify(authSessionManager).getCookie(request, "refreshToken");
            verify(tokenService).getToken(refreshToken);
            verify(tokenService).invalidateRefreshToken(testToken);
            verify(authSessionManager).invalidateSession(response);
        }

        @Test
        @DisplayName("Should handle logout when refresh token is missing")
        void shouldHandleLogoutWhenRefreshTokenIsMissing() {
            // Arrange
            when(authSessionManager.getCookie(request, "refreshToken")).thenReturn(null);
            doNothing().when(authSessionManager).invalidateSession(response);

            // Act
            authService.logout(request, response);

            // Assert
            verify(authSessionManager).getCookie(request, "refreshToken");
            verify(tokenService, never()).getToken(anyString());
            verify(authSessionManager).invalidateSession(response);
        }
    }
}

