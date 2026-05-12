package com.nexaworks.rafiq.entities;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.Specialization;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "consultation", indexes = {@Index(name = "doctor_idx", columnList = "doctor_id"),
        @Index(name = "patient_con_idx", columnList = "patient_id"),
        @Index(name = "status_idx", columnList = "status")}, uniqueConstraints = {
                @UniqueConstraint(name = "uk_doctor_date_start", columnNames = {"doctor_id",
                        "start_time"})})
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

    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL)
    private CancellationLog cancellationLog;

    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL)
    private Payment payment;

    private String accessToken;

    @Enumerated(EnumType.STRING)
    private Specialization specialization;

    @OneToOne(mappedBy = "consultation")
    private ConsultationSummary consultationSummary;

    @Transient
    public boolean isCancelled() {
        return status == ConsultationStatus.CANCELLED;
    }
}
