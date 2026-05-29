package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "feedback", indexes = {
        @Index(name = "idx_feedback_patient", columnList = "patient_id"),
        @Index(name = "idx_feedback_doctor", columnList = "doctor_id"),
        @Index(name = "idx_feedback_consultation", columnList = "consultation_id")})
public class Feedback extends BaseEntity {

    private String feedback;

    private double rating;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @OneToOne
    @JoinColumn(name = "consultation_id")
    private Consultation consultation;

}
