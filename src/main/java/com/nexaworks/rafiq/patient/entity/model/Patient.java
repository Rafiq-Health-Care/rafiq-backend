package com.nexaworks.rafiq.patient.entity.model;

import java.util.Date;
import java.util.List;

import com.nexaworks.rafiq.labTest.entity.LabTest;
import com.nexaworks.rafiq.patient.entity.enums.BloodType;
import com.nexaworks.rafiq.medication.entity.model.Group;
import com.nexaworks.rafiq.medication.entity.model.Medicine;
import com.nexaworks.rafiq.medication.entity.model.ReminderLog;
import com.nexaworks.rafiq.patient.entity.enums.SmokeStatus;
import com.nexaworks.rafiq.user.entity.model.User;

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

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE,
            CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WeightHistory> weightHistory;
}
