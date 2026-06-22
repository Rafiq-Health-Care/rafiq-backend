package com.nexaworks.rafiq.entities;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
@ToString(exclude = {"slot", "patient", "consultationSummary", "consultationLog", "cancellationLog",
        "payment"})
@Table(name = "consultation", indexes = {
        @Index(name = "idx_consultation_patient", columnList = "patient_id"),
        @Index(name = "idx_consultation_slot", columnList = "slot_id"),
        @Index(name = "idx_consultation_status", columnList = "status"),
        @Index(name = "idx_consultation_doctor", columnList = "doctor_id")})
public class Consultation extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "slot_id", nullable = false)
    private ConsultationSlot slot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.PENDING;

    @Column(unique = true)
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @OneToOne(mappedBy = "consultation", cascade = {CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST})
    private CancellationLog cancellationLog;

    @OneToOne(mappedBy = "consultation", cascade = {CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private ConsultationSummary consultationSummary;

    @OneToOne(mappedBy = "consultation", cascade = {CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private ConsultationLog consultationLog;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @OneToOne(mappedBy = "consultation", cascade = {CascadeType.REMOVE,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Feedback feedback;

    @Transient
    public boolean isCancelled() {
        return status == ConsultationStatus.CANCELLED;
    }

}
