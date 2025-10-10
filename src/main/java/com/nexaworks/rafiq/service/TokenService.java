package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Optional;

public interface TokenService {
    public String  generateRefreshToken(User user);

    String generateOtpToken(User user);

    Token getToken(String otp);

    String generateAccessToken(Optional<User> byEmail);

    User verifyOtp(String email, String otp);

    void invalidateRefreshToken(Token token);
}
