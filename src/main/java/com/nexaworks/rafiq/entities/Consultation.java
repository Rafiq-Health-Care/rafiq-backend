package com.nexaworks.rafiq.entities;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.PaymentStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
public class Consultation extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false, unique = true)
    private ConsultationSlot slot;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL)
    private Payment payment;

    @OneToOne(mappedBy = "consultation", cascade = CascadeType.ALL)
    private CancellationLog cancellationLog;

    @OneToOne(mappedBy = "consultation")
    private ConsultationSummary consultationSummary;
    @OneToOne(mappedBy = "consultation")
    private ConsultationLog consultationLog;

    @Transient
    public boolean isCancelled() {
        return status == ConsultationStatus.CANCELLED;
    }

    @Transient
    public boolean isRefundable() {
        return isCancelled() && payment != null && payment.getStatus() == PaymentStatus.SUCCEEDED;
    }

    @Transient
    public Doctor getDoctor() {
        return slot.getDoctor();
    }

}
