package com.nexaworks.rafiq.entities;

import java.util.Date;
import java.util.List;

import com.nexaworks.rafiq.entities.enums.BloodType;
import com.nexaworks.rafiq.entities.enums.SmokeStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
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
@Table(name = "patient")
public class Patient extends User {

    private String description;
    @PositiveOrZero
    private int height;

    @PositiveOrZero
    private double weight;

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
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE,
            CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WeightHistory> weightHistory;
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Consultation> consultations;
}
