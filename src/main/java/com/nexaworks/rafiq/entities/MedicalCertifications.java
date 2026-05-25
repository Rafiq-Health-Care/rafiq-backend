package com.nexaworks.rafiq.entities;

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
@SuperBuilder
@Entity
@Table(name = "medical_certifications", indexes = {
        @Index(name = "doctor_idx", columnList = "doctor_id")})
public class MedicalCertifications extends BaseEntity {

    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String code;
    // todo file mangement
    private String photo;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
}
