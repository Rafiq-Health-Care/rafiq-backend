package com.nexaworks.rafiq.entities;

import java.util.List;

import com.nexaworks.rafiq.entities.enums.Specialization;
import org.hibernate.annotations.BatchSize;

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
@Table(name = "doctor", indexes = {
        @Index(name = "specialization_idx", columnList = "specialization")})
public class Doctor extends User {
    private String description;
    private String hospitalName;
    private String personalPhoto;
    private String nationalId;
    private String hospitalId;

    @Enumerated(EnumType.STRING)
    private Specialization specialization;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<MedicalCertifications> medicalCertifications;

    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<LabTest> labTests;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "social_links_id", referencedColumnName = "id")
    private SocialLinks socialLinks;

    private String publicId;

    @Enumerated(EnumType.STRING)
    private Status status;
}
