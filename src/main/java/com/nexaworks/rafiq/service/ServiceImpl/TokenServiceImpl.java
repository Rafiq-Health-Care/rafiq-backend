package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.TokenType;
import com.nexaworks.rafiq.repository.TokenRepository;
import com.nexaworks.rafiq.service.TokenService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;
    @Value("${refresh.expiration}")
    private Long REFRESH_EXPIRATION;

    @Override
    public String generateRefreshToken(User user){
        if(user == null){
            throw new IllegalArgumentException("User cannot be null");
        }
        String refreshToken = UUID.randomUUID().toString();
        Token token = buildRefreshToken(user,refreshToken);
        log.info("Generated refresh token {}",refreshToken);
        tokenRepository.save(token);
        log.info("Saved refresh token {}",refreshToken);
        return refreshToken;
    }

    public Token buildRefreshToken(User user,String refreshToken) {
       return Token.builder().token(refreshToken).user(user)
                .tokenType(TokenType.REFRESH)
                .expiryDate(Instant.now().plusSeconds(REFRESH_EXPIRATION)).build();
    }

}
