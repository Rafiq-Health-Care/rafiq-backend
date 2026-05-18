package com.nexaworks.rafiq.entities;

import java.time.LocalDateTime;

import com.nexaworks.rafiq.entities.enums.SlotStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@SuperBuilder
public class ConsultationSlot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @OneToOne(mappedBy = "slot", fetch = FetchType.EAGER)
    private Consultation consultation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;
}
