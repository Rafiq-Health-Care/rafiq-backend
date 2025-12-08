package com.nexaworks.rafiq.doctor.entity.model;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.BatchSize;

import com.nexaworks.rafiq.doctor.entity.enums.DoctorStatus;
import com.nexaworks.rafiq.shared.entity.SocialLinks;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "doctor", indexes = {
        @Index(name = "specialization_idx", columnList = "specialization_id")})
public class Doctor {
    @Id
    private UUID id;
    private String description;
    private String hospitalName;
    private String personalPhoto;
    private String nationalId;
    private String hospitalId;
    private String firstName;
    private String lastName;
    private String email;

    @ManyToOne
    @JoinColumn(name = "specialization_id", nullable = false)
    private Specialization specialization;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<MedicalCertifications> medicalCertifications;
    @Embedded
    private SocialLinks socialLinks;
    private String publicId;

    @Enumerated(EnumType.STRING)
    private DoctorStatus status;
}
