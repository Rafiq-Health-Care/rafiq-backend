package com.nexaworks.rafiq.service.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.TokenType;
import com.nexaworks.rafiq.exception.custom.user.TokenInvalidException;
import com.nexaworks.rafiq.exception.custom.user.TokenNotFoundException;
import com.nexaworks.rafiq.exception.custom.user.UserException;
import com.nexaworks.rafiq.exception.custom.user.UserNotFoundException;
import com.nexaworks.rafiq.rabbit.manager.UserNotificationManager;
import com.nexaworks.rafiq.repository.TokenRepository;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.utils.TransactionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final TransactionUtils transactionUtils;
    private final UserNotificationManager manager;

    @Value("${refresh.expiration}")
    public Long REFRESH_EXPIRATION;

    @Value("${otp.expiration}")
    private Long OTP_EXPIRATION;

    @Value("${access.token.expiration}")
    private Long ACCESS_TOKEN_EXPIRATION;

    @Override
    @Transactional
    public String generateRefreshToken(User user) {
        if (user == null) {
            throw new UserException("User cannot be null");
        }
        String refreshToken = UUID.randomUUID().toString();
        Token token = buildToken(user, refreshToken, TokenType.REFRESH, REFRESH_EXPIRATION);
        log.info("Generated refresh token {}", refreshToken);
        tokenRepository.save(token);
        log.info("Saved refresh token {}", refreshToken);
        return refreshToken;
    }

    @Override
    @Transactional
    public String generateOtpToken(User user) {
        if (user == null) {
            throw new UserException("User cannot be null");
        }
        String otpToken = String.valueOf((int) (Math.random() * 900000) + 100000);
        Token token = buildToken(user, otpToken, TokenType.OTP, OTP_EXPIRATION);
        tokenRepository.save(token);
        return otpToken;
    }

    @Override
    public Token getToken(String otp) {
        return tokenRepository.findByToken(otp)
                .orElseThrow(() -> new TokenNotFoundException("Invalid Token"));
    }

    @Override
    @Transactional
    public String generateAccessToken(Optional<User> user) {
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        String accessToken = UUID.randomUUID().toString();
        Token token = buildToken(user.get(), accessToken, TokenType.ACCESS_TOKEN,
                ACCESS_TOKEN_EXPIRATION);
        tokenRepository.save(token);
        log.info("Saved access token {}", accessToken);
        return accessToken;
    }

    @Override
    @Transactional
    public User verifyOtp(String email, String otp) {
        Token token = tokenRepository.findByToken(otp)
                .orElseThrow(() -> new TokenNotFoundException("Invalid Token"));
        if (!token.getUser().getEmail().equals(email)
                || token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidException("Invalid OTP");
        }
        return token.getUser();
    }

    @Override
    @Transactional
    public void invalidateRefreshToken(Token token) {
        tokenRepository.delete(token);
    }

    @Override
    public void saveToken(Token token) {
        tokenRepository.save(token);
    }

    @Override
    @Transactional
    public void getNewOtp(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return;
        }
        List<Token> tokens = tokenRepository.findByTokenTypeAndUser(TokenType.OTP, user.get());
        if (tokens.size() > 5) {
            throw new UserException(
                    "You have reached the maximum number of OTPs allowed. Please try again later.");
        }
        tokens.stream().filter(token -> token.getExpiryDate().isAfter(LocalDateTime.now()))
                .forEach(token -> token.setExpiryDate(LocalDateTime.now()));

        String otp = generateOtpToken(user.get());
        log.info("Generated new OTP for {}", user.get().getEmail());

        transactionUtils.afterCommit(() -> manager.sendNewOtpEvent(user.get(), otp));
    }

    private Token buildToken(User user, String token, TokenType tokenType, Long EXPIRATION) {
        return Token.builder().token(token).user(user).tokenType(tokenType)
                .expiryDate(LocalDateTime.now().plusSeconds(EXPIRATION)).build();
    }

}
