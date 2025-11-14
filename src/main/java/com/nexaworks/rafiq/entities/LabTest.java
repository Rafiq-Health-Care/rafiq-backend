package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
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
public class LabTest extends BaseEntity {
    private String name;
    private String description;
    private String code;
    private String pdf;
    private String publicId;
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "lab_id", nullable = true)
    private Lab lab;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = true)
    private DoctorProfile doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = true)
    private PatientProfile patient;

    @OneToMany(mappedBy = "labTest", cascade = CascadeType.REMOVE)
    private List<LabResult> labResults;

    private Instant date;
}
