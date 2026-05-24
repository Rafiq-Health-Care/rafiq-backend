package com.nexaworks.rafiq.entities;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.nexaworks.rafiq.entities.enums.DoctorAcceptanceStatus;
import com.nexaworks.rafiq.entities.enums.Specialization;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@ToString(exclude = {"medicalCertifications", "labTests", "consultations"})
@Table(name = "doctor", indexes = {
        @Index(name = "specialization_idx", columnList = "specialization")})
public class Doctor extends User {
    @Column(length = 1000)
    private String description;

    @Column(name = "personal_photo")
    private String personalPhoto;

    @Column(name = "national_id") // national id photo
    private String nationalId;

    @Enumerated(EnumType.STRING)
    private DoctorAcceptanceStatus acceptanceStatus;

    @Enumerated(EnumType.STRING)
    private Specialization specialization;

    @OneToMany(mappedBy = "doctor", cascade = {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE}, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<MedicalCertifications> medicalCertifications;

    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<LabTest> labTests;

    @OneToOne(mappedBy = "doctor", cascade = {CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST})
    private SocialLinks socialLinks;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal price = BigDecimal.valueOf(1000);

    @Column(columnDefinition = "TEXT")
    private String biography;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Education> education;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<Experience> experience;

    @Column(name = "experience_years")
    private int experienceYears;

    @DecimalMax(value = "5.0", inclusive = true)
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(nullable = false, precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.valueOf(5);

    @Column(name = "balance", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.valueOf(0);

    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE,
            CascadeType.PERSIST, CascadeType.MERGE})
    @BatchSize(size = 10)
    private List<ConsultationSlot> consultations;
}
