package com.nexaworks.rafiq.entities;

import java.math.BigDecimal;

import com.nexaworks.rafiq.entities.enums.RefundStatus;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "refund_request", indexes = {@Index(name = "payment_idx", columnList = "payment_id"),
        @Index(name = "patient_idx", columnList = "patient_id"),
        @Index(name = "status_idx", columnList = "status"),
        @Index(name = "stripe_refund_idx", columnList = "stripe_refund_id")})
public class RefundRequest extends BaseEntity {

    @ToString.Include
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private RefundStatus status = RefundStatus.PENDING;
    @ToString.Include
    private BigDecimal amount;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @OneToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    private String stripeRefundId;

}
