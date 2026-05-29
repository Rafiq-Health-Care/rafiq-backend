package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@ToString(exclude = {"consultation", "cancelledBy"})
@Table(name = "cancellation_log", indexes = {
        @Index(name = "idx_cancellation_log_consultation", columnList = "consultation_id")})
public class CancellationLog extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by", nullable = false)
    private User cancelledBy;
}
