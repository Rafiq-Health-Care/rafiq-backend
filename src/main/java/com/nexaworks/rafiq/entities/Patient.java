package com.nexaworks.rafiq.entities;

import java.util.Date;
import java.util.List;

import org.hibernate.annotations.BatchSize;

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
@Table(name = "patient", indexes = {@Index(name = "patient_idx", columnList = "id")})
public class Patient extends User {

    @Column(columnDefinition = "TEXT")
    private String description;

    @PositiveOrZero
    private int height;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;

    @Enumerated(EnumType.STRING)
    private SmokeStatus smokeStatus;
    @PositiveOrZero
    private int cigarettesPerDay;
    private Date lastSmoked;

    private boolean alcoholism;
    @PositiveOrZero
    private int drinksPerWeek;

    private boolean pregnant;

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<LabTest> labTests;

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<Medicine> medicines;

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE, CascadeType.MERGE,
            CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<Group> groups;

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<ReminderLog> reminderLogs;

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE,
            CascadeType.MERGE}, fetch = FetchType.LAZY)
    @BatchSize(size = 10)
    private List<Consultation> consultations;
}
