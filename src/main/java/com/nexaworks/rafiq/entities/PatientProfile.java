package com.nexaworks.rafiq.entities;

import java.util.Date;
import java.util.List;

import com.nexaworks.rafiq.entities.enums.BloodType;
import com.nexaworks.rafiq.entities.enums.SmokeStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
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
@Table(name = "patient_profile", indexes = {
        @Index(name = "user_patient_idx", columnList = "user_id")})
public class PatientProfile extends BaseEntity {

    private String description;
    @Positive
    private int height;

    @Positive
    private int weight;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    private SmokeStatus smokeStatus;
    private int cigarettesPerDay;
    private Date lastSmoked;

    private boolean alcoholism;
    private int drinksPerWeek;

    private boolean pregnant;
    private String occupation;
    private String emergencyContactName;
    private String emergencyContactPhone;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<LabTest> labTests;
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Medicine> medicines;
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST}, fetch = FetchType.LAZY)
    private List<Group> groups;
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<ReminderLog> reminderLogs;
}
