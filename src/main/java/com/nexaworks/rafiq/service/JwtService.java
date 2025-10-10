package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.User;
import jakarta.validation.constraints.NotBlank;

public interface JwtService {

    String generateToken(User user);

    void invalidateJwtToken(@NotBlank String s);
}
