package com.nexaworks.rafiq.entities;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.nexaworks.rafiq.entities.enums.DoctorAcceptanceStatus;
import com.nexaworks.rafiq.entities.enums.Language;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.entities.enums.SubSpecialization;

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
        @Index(name = "specialization_idx", columnList = "specialization"),
        @Index(name = "doctor_idx", columnList = "id"),
        @Index(name = "doctor_acceptance_status_idx", columnList = "acceptance_status")})
public class Doctor extends User {
    @Column(length = 1000)
    private String description;

    @Column(name = "personal_photo")
    private String personalPhoto;

    @Column(name = "national_id") // national id photo
    private String nationalId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DoctorAcceptanceStatus acceptanceStatus = DoctorAcceptanceStatus.IN_REVIEW;

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
    private List<ConsultationSlot> consultationSlots;

    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY, cascade = {CascadeType.REMOVE,
            CascadeType.PERSIST, CascadeType.MERGE})
    @BatchSize(size = 10)
    private List<Consultation> consultations;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "doctor_sub_specialization", joinColumns = @JoinColumn(name = "doctor_id"), indexes = {
            @Index(name = "idx_doctor_sub_spec", columnList = "doctor_id, sub_specialization"),
            @Index(name = "idx_sub_spec", columnList = "sub_specialization") // ← for filtering
                                                                             // alone
    })
    @Enumerated(EnumType.STRING)
    @Column(name = "sub_specialization", nullable = false)
    private Set<SubSpecialization> subSpecializations = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "doctor_languages", joinColumns = @JoinColumn(name = "doctor_id"), indexes = {
            @Index(name = "idx_doctor_language", columnList = "doctor_id, language"),
            @Index(name = "idx_language", columnList = "language") // ← for filtering alone
    })
    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false)
    private Set<Language> languages = new HashSet<>();
}
