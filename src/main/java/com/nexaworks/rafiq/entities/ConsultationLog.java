package com.nexaworks.rafiq.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@ToString(exclude = "consultation")
@Table(name = "consultation_logs", indexes = {
        @Index(name = "idx_consultation_logs_consultation", columnList = "consultation_id"),
        @Index(name = "idx_consultation_logs_id", columnList = "id")})
public class ConsultationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id", nullable = false, unique = true)
    private Consultation consultation;

    @Column(name = "doctor_enter_time")
    private LocalDateTime doctorEnterTime;

    @Column(name = "doctor_leave_time")
    private LocalDateTime doctorLeaveTime;

    @Column(name = "patient_enter_time")
    private LocalDateTime patientEnterTime;

    @Column(name = "patient_leave_time")
    private LocalDateTime patientLeaveTime;

}