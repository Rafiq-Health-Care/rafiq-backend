package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.enums.TokenType;
import com.nexaworks.rafiq.exception.custom.TokenInvalidException;
import com.nexaworks.rafiq.exception.custom.TokenNotFoundException;
import com.nexaworks.rafiq.exception.custom.UserException;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.repository.TokenRepository;
import com.nexaworks.rafiq.service.TokenService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;
    @Value("${refresh.expiration}")
    public Long REFRESH_EXPIRATION;
    @Value("${otp.expiration}")
    private Long OTP_EXPIRATION;
    @Value("${access.token.expiration}")
    private Long ACCESS_TOKEN_EXPIRATION;


    @Override
    @Transactional
    public String generateRefreshToken(User user){
        if(user == null){
            throw new UserException("User cannot be null");
        }
        String refreshToken = UUID.randomUUID().toString();
        Token token = buildToken(user,refreshToken,TokenType.REFRESH, REFRESH_EXPIRATION);
        log.info("Generated refresh token {}",refreshToken);
        tokenRepository.save(token);
        log.info("Saved refresh token {}",refreshToken);
        return refreshToken;
    }

    @Override
    @Transactional
    public String generateOtpToken(User user) {
        String otpToken = String.valueOf(
                (int) (Math.random() * 900000) + 100000
        );
        Token token = buildToken(user,otpToken,TokenType.OTP,OTP_EXPIRATION);
        tokenRepository.save(token);
        return otpToken;
    }

    @Override
    public Token getToken(String otp) {
        return tokenRepository.findByToken(otp).orElseThrow(
                ()->new TokenNotFoundException("Invalid Token"));
    }

    @Override
    @Transactional
    public String generateAccessToken(Optional<User> user) {
        if (user.isEmpty()){
            throw new UserNotFoundException("User not found");
        }
        String accessToken = UUID.randomUUID().toString();
        Token token = buildToken(user.get(),accessToken,
                TokenType.ACCESS_TOKEN,ACCESS_TOKEN_EXPIRATION);
        tokenRepository.save(token);
        log.info("Saved access token {}",accessToken);
        return accessToken;
    }

    @Override
    public User verifyOtp(String email, String otp) {
        Token token = tokenRepository.findByToken(otp).orElseThrow(
                ()->new TokenNotFoundException("Invalid Token"));
        if (!token.getUser().getEmail().equals(email)
                ||token.getExpiryDate().isBefore(Instant.now())) {
            throw new TokenInvalidException("Invalid OTP");
        }
        return token.getUser();
    }

    @Override
    @Transactional
    public void invalidateRefreshToken(Token token) {
        tokenRepository.delete(token);
    }

    private Token buildToken(User user, String token, TokenType tokenType, Long EXPIRATION) {
       return Token.builder().token(token).user(user)
                .tokenType(tokenType)
                .expiryDate(Instant.now().plusSeconds(EXPIRATION)).build();
    }

}
