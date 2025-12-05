package com.nexaworks.rafiq.user.service;

import java.util.UUID;

import com.nexaworks.rafiq.user.api.dto.response.LoginResponse;
import com.nexaworks.rafiq.user.entity.model.User;

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
