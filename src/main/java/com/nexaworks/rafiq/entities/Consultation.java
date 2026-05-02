package com.nexaworks.rafiq.entities;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "consultation", indexes = {
        @Index(name = "doctor_idx", columnList = "doctor_id"),
        @Index(name = "patient_con_idx", columnList = "patient_id"),
        @Index(name = "status_idx", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_doctor_date_start",
                columnNames = {"doctor_id", "date", "start_time"})
})
public class Consultation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Embedded
    private TimeSlot timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL)
    private CancellationLog cancellationLog;

    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL)
    private Payment payment;


    @Transient
    public boolean isCancelled() {
        return status == ConsultationStatus.CANCELLED;
    }
}
