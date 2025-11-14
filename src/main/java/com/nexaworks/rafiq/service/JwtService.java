package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;

public interface JwtService {

  String generateToken(User user);

  void invalidateJwtToken(@NotBlank String s);

  Authentication validate(String jwt);
}
