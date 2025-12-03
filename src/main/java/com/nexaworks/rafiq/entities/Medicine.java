package com.nexaworks.rafiq.entities;

import java.time.Instant;
import java.util.List;

import com.nexaworks.rafiq.entities.enums.*;

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
@Table(name = "medicine", indexes = {
        @Index(columnList = "search_vector", name = "medicine_search_vector_idx"),
        @Index(columnList = "patient_id", name = "patient_medicine_idx"),
        @Index(columnList = "doctor_id", name = "doctor_medicine_idx")})
public class Medicine extends BaseEntity {

    @NotNull
    @Column(nullable = false)
    private String dosage;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MedicineFrequency frequency;

    @Enumerated(EnumType.STRING)
    private ReminderFrequency reminderFrequency;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "custom_days", joinColumns = @JoinColumn(name = "reminder_id"))
    @Column(name = "day")
    @Enumerated(EnumType.STRING)
    private List<Day> customDays;

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
    private String name;

    @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVector;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "drug_id", referencedColumnName = "id", nullable = false)
    private Drug drug;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", referencedColumnName = "id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;
    @OneToOne(mappedBy = "medicine", cascade = {CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST}, orphanRemoval = true)
    private Reminder reminder;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return drug.getId().equals(((Medicine) obj).drug.getId());
    }
}
