package com.nexaworks.rafiq.entities;

import java.util.List;

import com.nexaworks.rafiq.entities.enums.Status;

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

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.REMOVE)
    private List<MedicalCertifications> medicalCertifications;

    @OneToMany(mappedBy = "doctor")
    private List<LabTest> labTests;

    @OneToOne
    @JoinColumn(name = "social_links_id", referencedColumnName = "id")
    private SocialLinks socialLinks;

    private String publicId;

    @Enumerated(EnumType.STRING)
    private Status status;
}
