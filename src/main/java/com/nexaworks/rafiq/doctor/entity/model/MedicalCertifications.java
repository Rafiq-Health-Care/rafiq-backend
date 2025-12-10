package com.nexaworks.rafiq.doctor.entity.model;

import com.nexaworks.rafiq.shared.entity.BaseEntity;

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
@Table(name = "medical_certifications", schema = "doctor_schema")
public class MedicalCertifications extends BaseEntity {

    private String name;
    private String description;
    private String code;
    private String photo;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
}
