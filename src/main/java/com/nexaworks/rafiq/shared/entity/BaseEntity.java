package com.nexaworks.rafiq.shared.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@SQLDelete(sql = "UPDATE {h-schema}{h-table} SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false, nullable = false)
    private UUID createdBy;

    @LastModifiedBy
    private UUID updatedBy;

    @Column(name = "deleted_at")
    private Instant deletedAt;
}
