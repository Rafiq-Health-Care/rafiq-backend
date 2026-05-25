package com.nexaworks.rafiq.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.nexaworks.rafiq.rabbit.enums.DLQAction;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "message_log")
@Entity
public class MessageLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "source_queue", nullable = false)
    private String sourceQueue;

    @Column(name = "failure_reason", nullable = false, columnDefinition = "TEXT")
    private String failureReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "dlq_action", nullable = false)
    private DLQAction dlqAction;

    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "headers", columnDefinition = "jsonb")
    private String headers;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "redriven_at")
    private LocalDateTime redrivenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
