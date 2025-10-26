package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.TokenType;
import com.nexaworks.rafiq.exception.custom.UserException;
import com.nexaworks.rafiq.repository.TokenRepository;
import com.nexaworks.rafiq.service.ServiceImpl.TokenServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TokenServiceTest {
    @Mock
    TokenRepository tokenRepository;
    @InjectMocks
    TokenServiceImpl tokenService;
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(tokenService, "REFRESH_EXPIRATION", 3600L);
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
}
