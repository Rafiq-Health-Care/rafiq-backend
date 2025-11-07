package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.TokenType;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.exception.custom.TokenNotFoundException;
import com.nexaworks.rafiq.exception.custom.UserException;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.repository.TokenRepository;
import com.nexaworks.rafiq.service.ServiceImpl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void generateRefreshToken_ShouldThrowException_WhenUserIsNull(){
        assertThrows(UserException.class, () -> tokenService.generateRefreshToken(null));
        verify(tokenRepository, never()).save(any(Token.class));
    }

    @DisplayName("Build refresh token should return correct token object")
    @Test
    void buildRefreshToken_ShouldReturnCorrectTokenObject(){
        User user = new User();
        String refreshToken = UUID.randomUUID().toString();
        Token token = tokenService.buildToken(user,refreshToken,TokenType.REFRESH, tokenService.REFRESH_EXPIRATION);
        assertNotNull(token);
        assertEquals(user,token.getUser());
        assertEquals(refreshToken,token.getToken());
        assertEquals(TokenType.REFRESH,token.getTokenType());
    }

    @DisplayName("Generate access token should return and save access token")
    @Test
    void generateAccessToken_ShouldReturnAndSaveAccessToken(){
        User user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
        when(tokenRepository.save(any(Token.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Token fakeToken = new Token();
        fakeToken.setToken("fake-access-token");
        doReturn(fakeToken)
                .when(tokenService)
                .buildToken(eq(user), anyString(), any(TokenType.class), anyLong());
        String accessToken = tokenService.generateAccessToken(Optional.of(user));
        assertNotNull(accessToken);
        verify(tokenRepository,times(1)).save(any(Token.class));
    }

    @DisplayName("Generate access token should throw exception when user is null")
    @Test
    void generateAccessToken_ShouldThrowException_WhenUserIsNull(){
      assertThrows(UserNotFoundException.class, () -> tokenService.generateAccessToken(Optional.empty()));
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
        assertEquals(user,verifiedUser);

    }
    @DisplayName("Verify otp should throw exception when no otp token match")
    @Test
    void verifyOtp_ShouldThrowException_WhenNoOtpTokenMatch() {
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        assertThrows(TokenNotFoundException.class,
                () -> tokenService.verifyOtp("john@gmail.com", "1234"));
        verify(tokenRepository, times(1)).findByToken(anyString());
    }
    @DisplayName("Verify otp should throw exception when the otp is expired")
    @Test
    void verifyOtp_shouldThrowException_WhenOtpTokenIsExpired(){
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

}
