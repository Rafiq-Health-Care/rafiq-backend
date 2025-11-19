package com.nexaworks.rafiq.entities;

import java.time.Instant;
import java.util.List;

import com.nexaworks.rafiq.enums.MedicineFrequency;
import com.nexaworks.rafiq.enums.MedicineStatus;
import com.nexaworks.rafiq.enums.MedicineType;

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
public class Medicine extends BaseEntity {

    private String dosage;
    @Enumerated(EnumType.STRING)
    private MedicineFrequency frequency;
    @Enumerated(EnumType.STRING)
    private MedicineStatus status;
    @Enumerated(EnumType.STRING)
    private MedicineType type;
    private Instant startDate;
    private Instant endDate;
    private String notes;
    private String photoUrl;
    private String photoPublicId;
    @ManyToOne
    @JoinColumn(name = "drug_id", referencedColumnName = "id", nullable = false)
    private Drug drug;
    @ManyToOne
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private DoctorProfile doctor;
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private PatientProfile patient;
    @ManyToMany
    @JoinTable(name = "medicine_groups", joinColumns = @JoinColumn(name = "medicine_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
    private List<Group> groups;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return drug.getId().equals(((Medicine) obj).drug.getId());
    }
}
