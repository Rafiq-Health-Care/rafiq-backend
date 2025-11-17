package com.nexaworks.rafiq.service;

import java.util.Optional;

import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;

public interface TokenService {
    public String generateRefreshToken(User user);

    String generateOtpToken(User user);

    Token getToken(String otp);

    String generateAccessToken(Optional<User> byEmail);

    User verifyOtp(String email, String otp);

    void invalidateRefreshToken(Token token);

    void saveToken(Token token);
}
