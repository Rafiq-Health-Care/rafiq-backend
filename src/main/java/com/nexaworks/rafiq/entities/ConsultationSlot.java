package com.nexaworks.rafiq.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import com.nexaworks.rafiq.entities.enums.SlotStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString(exclude = {"doctor", "consultations"})
@SuperBuilder
@Table(name = "consultation_slot", indexes = {@Index(name = "doctor_idx", columnList = "doctor_id"),
        @Index(name = "status_idx", columnList = "status"),
        @Index(name = "slot_idx", columnList = "id"),
        @Index(name = "time_idx", columnList = "start_time,end_time")})
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

    @OneToMany(mappedBy = "slot", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE,
            CascadeType.MERGE, CascadeType.PERSIST})
    @BatchSize(size = 10)
    private List<Consultation> consultations;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SlotStatus status = SlotStatus.AVAILABLE;
}
