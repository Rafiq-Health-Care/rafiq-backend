package com.nexaworks.rafiq.entities;

import com.nexaworks.rafiq.enums.TokenType;
import jakarta.persistence.*;
import java.time.Instant;
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
public class Token extends BaseEntity {

    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private Instant expiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
