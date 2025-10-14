package com.nexaworks.rafiq.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class DoctorProfile extends BaseEntity {
    private String description;
    private String hospitalName;
    private String personalPhoto;
    private String nationalId;
    private String hospitalId;
    @OneToOne(mappedBy = "doctorProfile")
    private User user;
    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;

    @OneToMany(mappedBy = "doctor",cascade = CascadeType.REMOVE)
    private List<MedicalCertifications> medicalCertifications;
    @OneToMany(mappedBy = "doctor")
    private List<LabTest> labTests;



}
