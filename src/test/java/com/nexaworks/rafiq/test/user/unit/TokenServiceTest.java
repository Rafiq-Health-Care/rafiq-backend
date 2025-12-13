package com.nexaworks.rafiq.test.user.unit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.shared.event.user.NewOtpEvent;
import com.nexaworks.rafiq.user.entity.enums.TokenType;
import com.nexaworks.rafiq.user.entity.model.Token;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.exception.TokenInvalidException;
import com.nexaworks.rafiq.user.exception.TokenNotFoundException;
import com.nexaworks.rafiq.user.exception.UserException;
import com.nexaworks.rafiq.user.exception.UserNotFoundException;
import com.nexaworks.rafiq.user.repository.TokenRepository;
import com.nexaworks.rafiq.user.repository.UserRepository;
import com.nexaworks.rafiq.user.service.implementation.TokenServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService Unit Tests")
class TokenServiceTest {

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    @Spy
    private TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenService, "REFRESH_EXPIRATION", 3600L);
        ReflectionTestUtils.setField(tokenService, "OTP_EXPIRATION", 3600L);
        ReflectionTestUtils.setField(tokenService, "ACCESS_TOKEN_EXPIRATION", 3600L);
        TransactionSynchronizationManager.initSynchronization();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clearSynchronization();
    }

    private void triggerTransactionSynchronization() {
        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> {
            try {
                sync.afterCommit();
            } catch (Exception e) {
                // Ignore exceptions in test
            }
        });
    }

    @DisplayName("Generate refresh token should return token and save to repository")
    @Test
    void generateRefreshToken_ShouldReturnTokenAndSaveToRepository() {
        // Arrange
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();

        when(tokenRepository.save(any(Token.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String refreshToken = tokenService.generateRefreshToken(user);

        // Assert
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        verify(tokenRepository, times(1)).save(any(Token.class));
        verify(tokenRepository).save(argThat(savedToken -> savedToken.getUser().equals(user)
                && savedToken.getTokenType() == TokenType.REFRESH
                && savedToken.getExpiryDate().isAfter(Instant.now())));
    }

    @DisplayName("Generate refresh token should throw exception when user is null")
    @Test
    void generateRefreshToken_ShouldThrowException_WhenUserIsNull() {
        // Act & Assert
        assertThrows(UserException.class, () -> tokenService.generateRefreshToken(null));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @DisplayName("Generate access token must build the token and return it")
    @Test
    void generateAccessToken_ShouldBuildTheTokenAndReturnIt() {
        // Arrange
        User user = User.builder().firstName("John").lastName("Doe").build();
        user.setEmail("john@gmail.com");

        when(tokenRepository.save(any(Token.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String accessToken = tokenService.generateAccessToken(Optional.of(user));

        // Assert
        assertNotNull(accessToken);
        assertFalse(accessToken.isEmpty());
        verify(tokenRepository, times(1)).save(any(Token.class));
        verify(tokenRepository).save(argThat(savedToken -> savedToken.getUser().equals(user)
                && savedToken.getTokenType() == TokenType.ACCESS_TOKEN
                && savedToken.getExpiryDate().isAfter(Instant.now())));
    }

    @DisplayName("Generate access token should throw exception when user is null")
    @Test
    void generateAccessToken_ShouldThrowException_WhenUserIsNull() {
        // Act & Assert
        assertThrows(UserNotFoundException.class,
                () -> tokenService.generateAccessToken(Optional.empty()));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @DisplayName("Verify otp should return the user when otp is valid")
    @Test
    void verifyOtp_ShouldReturnTheUser_WhenOtpIsValid() {
        // Arrange
        User user = User.builder().firstName("John").lastName("Doe").build();
        user.setEmail("john@gmail.com");

        Token token = new Token();
        token.setUser(user);
        token.setToken("123456");
        token.setTokenType(TokenType.OTP);
        token.setExpiryDate(Instant.now().plusSeconds(3600));

        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        // Act
        User verifiedUser = tokenService.verifyOtp(user.getEmail(), "123456");

        // Assert
        assertNotNull(verifiedUser);
        assertEquals(user, verifiedUser);
        verify(tokenRepository, times(1)).findByToken("123456");
    }

    @DisplayName("Verify otp should throw exception when no otp token match")
    @Test
    void verifyOtp_ShouldThrowException_WhenNoOtpTokenMatch() {
        // Arrange
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TokenNotFoundException.class,
                () -> tokenService.verifyOtp("john@gmail.com", "1234"));
        verify(tokenRepository, times(1)).findByToken("1234");
    }

    @DisplayName("Verify otp should throw exception when the otp is expired")
    @Test
    void verifyOtp_shouldThrowException_WhenOtpTokenIsExpired() {
        // Arrange
        User user = User.builder().firstName("John").lastName("Doe").build();
        user.setEmail("john@gmail.com");

        Token token = new Token();
        token.setUser(user);
        token.setToken("123456");
        token.setTokenType(TokenType.OTP);
        token.setExpiryDate(Instant.now().minusSeconds(1));

        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        // Act & Assert
        assertThrows(TokenInvalidException.class,
                () -> tokenService.verifyOtp(user.getEmail(), "123456"));
        verify(tokenRepository, times(1)).findByToken("123456");
    }

    @DisplayName("Verify otp should throw an exception if the email doesn't match")
    @Test
    void verifyOtp_shouldThrowException_WhenEmailDoesNotMatch() {
        // Arrange
        User user = User.builder().firstName("John").lastName("Doe").build();
        user.setEmail("john@gmail.com");

        Token token = new Token();
        token.setUser(user);
        token.setToken("123456");
        token.setTokenType(TokenType.OTP);
        token.setExpiryDate(Instant.now().plusSeconds(3600));

        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        // Act & Assert
        assertThrows(TokenInvalidException.class,
                () -> tokenService.verifyOtp("elbialy@gmail.com", "123456"));
        verify(tokenRepository, times(1)).findByToken("123456");
    }

    @DisplayName("Invalidate refresh token should delete the token from repository")
    @Test
    void invalidateRefreshToken_ShouldDeleteTheTokenFromRepository() {
        // Arrange
        Token token = new Token();
        doNothing().when(tokenRepository).delete(any(Token.class));

        // Act
        tokenService.invalidateRefreshToken(token);

        // Assert
        verify(tokenRepository, times(1)).delete(token);
    }

    @DisplayName("Get token should return the token if it exists")
    @Test
    void getToken_ShouldReturnTheToken_IfItExists() {
        // Arrange
        Token token = new Token();
        token.setToken("123456");

        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));

        // Act
        Token foundToken = tokenService.getToken("123456");

        // Assert
        assertNotNull(foundToken);
        assertEquals(token, foundToken);
        verify(tokenRepository, times(1)).findByToken("123456");
    }

    @DisplayName("Get token should throw exception if it doesn't exist")
    @Test
    void getToken_ShouldThrowException_IfItDoesNotExist() {
        // Arrange
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TokenNotFoundException.class, () -> tokenService.getToken("123456"));
        verify(tokenRepository, times(1)).findByToken("123456");
    }

    @DisplayName("Generate otp token should generate token and save it")
    @Test
    void generateOtpToken_ShouldGenerateTokenAndSaveIt() {
        // Arrange
        User user = new User();
        user.setEmail("test@gmail.com");

        when(tokenRepository.save(any(Token.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String otpToken = tokenService.generateOtpToken(user);

        // Assert
        assertNotNull(otpToken);
        assertFalse(otpToken.isEmpty());
        verify(tokenRepository, times(1)).save(any(Token.class));
        verify(tokenRepository).save(argThat(savedToken -> savedToken.getUser().equals(user)
                && savedToken.getTokenType() == TokenType.OTP
                && savedToken.getExpiryDate().isAfter(Instant.now())));
    }

    @DisplayName("Generate otp token should throw user exception if the user is null")
    @Test
    void generateOtpToken_ShouldThrowUserException_WhenUserIsNull() {
        // Act & Assert
        assertThrows(UserException.class, () -> tokenService.generateOtpToken(null));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @DisplayName("Get new OTP should generate OTP and publish event when user exists")
    @Test
    void getNewOtp_ShouldGenerateOtpAndPublishEvent_WhenUserExists() {
        // Arrange
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").firstName("John")
                .lastName("Doe").build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findByTokenTypeAndUser(TokenType.OTP, user))
                .thenReturn(new java.util.ArrayList<>());
        when(tokenRepository.save(any(Token.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        tokenService.getNewOtp(user.getEmail());
        triggerTransactionSynchronization();

        // Assert
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(tokenRepository, times(1)).save(any(Token.class));

        ArgumentCaptor<NewOtpEvent> eventCaptor = ArgumentCaptor.forClass(NewOtpEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        NewOtpEvent capturedEvent = eventCaptor.getValue();
        assertEquals(user.getEmail(), capturedEvent.email());
        assertEquals(user.getFirstName(), capturedEvent.name());
        assertNotNull(capturedEvent.otp());
        assertFalse(capturedEvent.otp().isEmpty());
    }

    @DisplayName("Get new OTP should return silently when user does not exist")
    @Test
    void getNewOtp_ShouldReturnSilently_WhenUserDoesNotExist() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        tokenService.getNewOtp(email);
        triggerTransactionSynchronization();

        // Assert
        verify(userRepository, times(1)).findByEmail(email);
        verify(tokenRepository, never()).save(any(Token.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @DisplayName("Get new OTP should throw exception when maximum OTP limit is reached")
    @Test
    void getNewOtp_ShouldThrowException_WhenMaximumOtpLimitReached() {
        // Arrange
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").firstName("John")
                .build();

        List<Token> tokens = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            tokens.add(new Token());
        }

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(tokenRepository.findByTokenTypeAndUser(TokenType.OTP, user)).thenReturn(tokens);

        // Act & Assert
        assertThrows(UserException.class, () -> tokenService.getNewOtp(user.getEmail()));
        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(tokenRepository, never()).save(any(Token.class));
        verify(eventPublisher, never()).publishEvent(any());
    }
}
