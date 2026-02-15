package com.nexaworks.rafiq.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.TokenType;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.exception.custom.TokenNotFoundException;
import com.nexaworks.rafiq.exception.custom.UserException;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.repository.TokenRepository;
import com.nexaworks.rafiq.service.user.TokenServiceImpl;

@DisplayName("TokenService Test Cases")
public class TokenServiceTest {
    @Mock
    TokenRepository tokenRepository;

    @InjectMocks
    @Spy
    TokenServiceImpl tokenService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(tokenService, "REFRESH_EXPIRATION", 3600L);
        ReflectionTestUtils.setField(tokenService, "OTP_EXPIRATION", 3600L);
        ReflectionTestUtils.setField(tokenService, "ACCESS_TOKEN_EXPIRATION", 3600L);
    }

    @DisplayName("Generate refresh token should return token and save to repository")
    @Test
    void generateRefreshToken_ShouldReturnTokenAndSaveToRepository() {
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        when(tokenRepository.save(any(Token.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String refreshToken = tokenService.generateRefreshToken(user);
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @DisplayName("Generate refresh token should throw exception when user is null")
    @Test
    void generateRefreshToken_ShouldThrowException_WhenUserIsNull() {
        assertThrows(UserException.class, () -> tokenService.generateRefreshToken(null));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @DisplayName("Generate token must build the token and return it")
    @Test
    void generateAccessToken_ShouldBuildTheTokenAndReturnIt() {
        User user = User.builder().firstName("John").lastName("Doe").build();
        user.setEmail("john@gmail.com");
        when(tokenRepository.save(any(Token.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        String accessToken = tokenService.generateAccessToken(Optional.of(user));
        assertNotNull(accessToken);
        assertFalse(accessToken.isEmpty());
        verify(tokenRepository, times(1)).save(any(Token.class));
        verify(tokenRepository).save(argThat(savedToken -> savedToken.getUser().equals(user)
                && savedToken.getTokenType() == TokenType.ACCESS_TOKEN
                && savedToken.getExpiryDate().isAfter(java.time.Instant.now())));
    }

    @DisplayName("Generate access token should throw exception when user is null")
    @Test
    void generateAccessToken_ShouldThrowException_WhenUserIsNull() {
        assertThrows(UserNotFoundException.class,
                () -> tokenService.generateAccessToken(Optional.empty()));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @DisplayName("Verify otp should return the user when otp is valid")
    @Test
    void verifyOtp_ShouldReturnTheUser_WhenOtpIsValid() {
        User user = User.builder().firstName("John").lastName("Doe").build();
        user.setEmail("john@gmail.com");
        Token token = new Token();
        token.setUser(user);
        token.setToken("123456");
        token.setTokenType(TokenType.OTP);
        token.setExpiryDate(java.time.Instant.now().plusSeconds(3600));
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        User verifiedUser = tokenService.verifyOtp(user.getEmail(), "123456");
        assertNotNull(verifiedUser);
        assertEquals(user, verifiedUser);
    }

    @DisplayName("Verify otp should throw exception when no otp token match")
    @Test
    void verifyOtp_ShouldThrowException_WhenNoOtpTokenMatch() {
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        assertThrows(TokenNotFoundException.class, () -> tokenService.verifyOtp("john@gmail.com", "1234"));
        verify(tokenRepository, times(1)).findByToken(anyString());
    }

    @DisplayName("Verify otp should throw exception when the otp is expired")
    @Test
    void verifyOtp_shouldThrowException_WhenOtpTokenIsExpired() {
        User user = User.builder().firstName("John").lastName("Doe").build();
        user.setEmail("john@gmail.com");
        Token token = new Token();
        token.setUser(user);
        token.setToken("123456");
        token.setTokenType(TokenType.OTP);
        token.setExpiryDate(java.time.Instant.now());
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        assertThrows(TokenInvalidException.class,
                () -> tokenService.verifyOtp(user.getEmail(), "123456"));
        verify(tokenRepository, times(1)).findByToken(anyString());
    }

    @DisplayName("Verify otp should throw an exception if the email doesn't match")
    @Test
    void verifyOtp_shouldThrowException_WhenEmailDoesNotMatch() {
        User user = User.builder().firstName("John").lastName("Doe").build();
        user.setEmail("john@gmail.com");
        Token token = new Token();
        token.setUser(user);
        token.setToken("123456");
        token.setTokenType(TokenType.OTP);
        token.setExpiryDate(java.time.Instant.now());
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        assertThrows(TokenInvalidException.class,
                () -> tokenService.verifyOtp("elbialy@gmail.com", "123456"));
        verify(tokenRepository, times(1)).findByToken(anyString());
    }

    @DisplayName("Invalidate refresh token should delete the token from repository")
    @Test
    void invalidateRefreshToken_ShouldDeleteTheTokenFromRepository() {
        Token token = new Token();
        doNothing().when(tokenRepository).delete(any(Token.class));
        tokenService.invalidateRefreshToken(token);
        verify(tokenRepository, times(1)).delete(any(Token.class));
    }

    @DisplayName("Get token should return the token if it exists")
    @Test
    void getToken_ShouldReturnTheToken_IfItExists() {
        Token token = new Token();
        token.setToken("123456");
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        Token foundToken = tokenService.getToken("123456");
        assertNotNull(foundToken);
        assertEquals(token, foundToken);
        verify(tokenRepository, times(1)).findByToken(anyString());
    }

    @DisplayName("Get token should throw exception if it doesn't exist")
    @Test
    void getToken_ShouldThrowException_IfItDoesNotExist() {
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        assertThrows(TokenNotFoundException.class, () -> tokenService.getToken("123456"));
    }

    @DisplayName("Generate otp token should generate token and save it")
    @Test
    void generateOtpToken_ShouldGenerateTokenAndSaveIt() {
        User user = new User();
        user.setEmail("test@gmail.com");
        when(tokenRepository.save(any(Token.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        String otpToken = tokenService.generateOtpToken(user);
        assertNotNull(otpToken);
        assertFalse(otpToken.isEmpty());
        verify(tokenRepository, times(1)).save(any(Token.class));
        verify(tokenRepository).save(argThat(savedToken -> savedToken.getUser().equals(user)
                && savedToken.getTokenType() == TokenType.OTP
                && savedToken.getExpiryDate().isAfter(java.time.Instant.now())));
    }

    @DisplayName("Generate otp token should throw user exception if the user is null")
    @Test
    void generateOtpToken_ShouldThrowUserException_WhenUserIsNull() {
        assertThrows(UserException.class, () -> tokenService.generateOtpToken(null));
        verify(tokenRepository, never()).save(any(Token.class));
    }
}
