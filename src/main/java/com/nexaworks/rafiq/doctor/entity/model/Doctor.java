package com.nexaworks.rafiq.doctor.entity.model;

import java.util.List;

import com.nexaworks.rafiq.labTest.entity.LabTest;
import com.nexaworks.rafiq.shared.entity.SocialLinks;
import org.hibernate.annotations.BatchSize;

import com.nexaworks.rafiq.doctor.entity.enums.DoctorStatus;
import com.nexaworks.rafiq.user.entity.model.User;

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
        @Index(name = "specialization_idx", columnList = "specialization_id")})
public class Doctor extends User {
    private String description;
    private String hospitalName;
    private String personalPhoto;
    private String nationalId;
    private String hospitalId;

    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<MedicalCertifications> medicalCertifications;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "social_links_id", referencedColumnName = "id")
    private SocialLinks socialLinks;

    private String publicId;

    @Enumerated(EnumType.STRING)
    private DoctorStatus status;
}
