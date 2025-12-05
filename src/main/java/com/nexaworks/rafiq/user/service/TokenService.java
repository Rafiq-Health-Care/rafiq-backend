package com.nexaworks.rafiq.user.service;

import java.util.Optional;

import com.nexaworks.rafiq.user.entity.model.Token;
import com.nexaworks.rafiq.user.entity.model.User;

public interface TokenService {
    String generateRefreshToken(User user);

    String generateOtpToken(User user);

    Token getToken(String otp);

    String generateAccessToken(Optional<User> byEmail);

    User verifyOtp(String email, String otp);

    void invalidateRefreshToken(Token token);

    void saveToken(Token token);
    void getNewOtp(String email);
}
