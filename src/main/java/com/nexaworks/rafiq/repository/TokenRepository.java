package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Token;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, UUID> {
    Optional<Token> findByToken(String otp);
}
