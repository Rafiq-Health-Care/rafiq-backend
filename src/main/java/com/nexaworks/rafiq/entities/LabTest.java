package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class LabTest extends BaseEntity{
    private String name;
    private String description;
    private String code;
    private String pdf;
    @ManyToOne
    @JoinColumn(name = "lab_id")
    private Lab lab;
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private DoctorProfile doctor;
    @ManyToOne
    @JoinColumn(name = "patient_id")
    private PatientProfile patient;
    @OneToMany(mappedBy = "labTest")
    private List<LabResult> labResults;


}
