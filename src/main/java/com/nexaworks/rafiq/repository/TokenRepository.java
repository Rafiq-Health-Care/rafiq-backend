package com.nexaworks.rafiq.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.Token;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.entities.enums.TokenType;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByToken(String otp);

    List<Token> findByTokenTypeAndUser(TokenType tokenType, User user);
}
