package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class MedicalCertifications extends BaseEntity{
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String description;
    private String code;
    private String photo;

    @ManyToOne
    @JoinColumn(name = "doctor_id",nullable = false)
    private DoctorProfile doctor;
}
