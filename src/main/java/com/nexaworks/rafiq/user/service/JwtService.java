package com.nexaworks.rafiq.user.service;

import org.springframework.security.core.Authentication;

import com.nexaworks.rafiq.user.entity.model.User;

import jakarta.validation.constraints.NotBlank;

public interface JwtService {

    String generateToken(User user);

    void invalidateJwtToken(@NotBlank String s);

    Authentication validate(String jwt);
}
