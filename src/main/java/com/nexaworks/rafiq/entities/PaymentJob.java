package com.nexaworks.rafiq.entities;

import com.nexaworks.rafiq.entities.enums.JobStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "payment_jobs", indexes = {
        @Index(name = "idx_job_status_run_at", columnList = "status, run_at")
})
public class PaymentJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status;

    @Column(name = "run_at", nullable = false)
    private LocalDateTime runAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        runAt = LocalDateTime.now().plusMinutes(10);
        status = JobStatus.PENDING;
    }
}