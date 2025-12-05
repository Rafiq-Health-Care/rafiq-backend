package com.nexaworks.rafiq.user.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.user.entity.model.Token;
import com.nexaworks.rafiq.user.entity.model.User;
import com.nexaworks.rafiq.user.entity.enums.TokenType;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByToken(String otp);

    List<Token> findByTokenTypeAndUser(TokenType tokenType, User user);
}
