package com.nexaworks.rafiq.unit.service;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.JwtService;
import com.nexaworks.rafiq.service.TokenService;
import com.nexaworks.rafiq.service.authentication.implementaion.AuthServiceImpl;
import com.nexaworks.rafiq.utils.AuthSessionManager;

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
                    .isInstanceOf(TokenInvalidException.class).hasMessage("Invalid Refresh Token");
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
        }
    }
}
