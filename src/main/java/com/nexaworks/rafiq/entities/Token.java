package com.nexaworks.rafiq.entities;

import java.time.Instant;

import com.nexaworks.rafiq.entities.enums.TokenType;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "token", indexes = {@Index(name = "token_idx", columnList = "token"),
        @Index(name = "user_idx", columnList = "user_id")})
public class Token extends BaseEntity {

    @NotBlank
    @Column(unique = true, nullable = false, length = 1000)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private Instant expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
}
