package com.nexaworks.rafiq.entities;

import java.math.BigDecimal;
import java.time.Instant;

import com.nexaworks.rafiq.entities.enums.PayoutStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@ToString(exclude = {"consultation", "doctor"})
@Table(name = "payout", indexes = {
        @Index(name = "idx_payout_consultation", columnList = "consultation_id"),
        @Index(name = "idx_payout_doctor", columnList = "doctor_id"),
        @Index(name = "idx_payout_status", columnList = "status"),
        @Index(name = "idx_payout_release_at", columnList = "release_at")})
public class Payout extends BaseEntity {

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(nullable = false)
    private Instant releaseAt;

    private Instant paidAt;

    private String payoutIntentId;
}
