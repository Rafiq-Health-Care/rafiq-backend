package com.nexaworks.rafiq.patient.entity.model;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "weight_history", schema = "patient_schema")
public class WeightHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private double weight;
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;
}
