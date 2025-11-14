package com.nexaworks.rafiq.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.Token;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByToken(String otp);
}
