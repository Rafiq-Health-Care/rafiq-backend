package com.nexaworks.rafiq.service.authentication;

import java.util.UUID;

import com.nexaworks.rafiq.dto.response.auth.LoginResponse;
import com.nexaworks.rafiq.entities.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;

public interface AuthService {
    User getAuthenticateUser();

    LoginResponse login(@NotBlank String email, @NotBlank String password,
            HttpServletResponse response);

    LoginResponse refresh(HttpServletResponse response, HttpServletRequest request);

    void logout(HttpServletRequest request, HttpServletResponse response);

    UUID getAuthenticateUserId();
}
