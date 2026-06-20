package com.nexaworks.rafiq.entities;

import java.time.LocalDateTime;

import com.nexaworks.rafiq.entities.enums.TokenType;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(exclude = "user")
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, of = "token")
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "token", indexes = {@Index(name = "idx_token_token", columnList = "token"),
        @Index(name = "idx_token_user", columnList = "user_id")})
public class Token extends BaseEntity {

    @NotBlank
    @Column(unique = true, nullable = false, length = 1000)
    private String token;

    @Enumerated(EnumType.STRING)
    private TokenType tokenType;

    private LocalDateTime expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
