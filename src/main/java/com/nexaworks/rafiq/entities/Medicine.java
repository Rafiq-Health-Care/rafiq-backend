package com.nexaworks.rafiq.entities;

import java.time.Instant;
import java.util.Set;

import com.nexaworks.rafiq.enums.MedicineFrequency;
import com.nexaworks.rafiq.enums.MedicineStatus;
import com.nexaworks.rafiq.enums.MedicineType;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
public class Medicine extends BaseEntity {

    @NotNull
    @Column(nullable = false)
    private String dosage;
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MedicineFrequency frequency;
    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MedicineStatus status = MedicineStatus.ACTIVE;
    @Enumerated(EnumType.STRING)
    private MedicineType type;
    private Instant startDate;
    private Instant endDate;
    private String notes;
    private String photoUrl;
    private String photoPublicId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "drug_id", referencedColumnName = "id", nullable = false)
    private Drug drug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private DoctorProfile doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private PatientProfile patient;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "medicine_groups", joinColumns = @JoinColumn(name = "medicine_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Set<Group> groups;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return drug.getId().equals(((Medicine) obj).drug.getId());
    }
}
