package com.nexaworks.rafiq.patient.entity.model;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.patient.entity.enums.BloodType;
import com.nexaworks.rafiq.patient.entity.enums.SmokeStatus;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "patient")
public class Patient {

    @Id
    private UUID id;

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

    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE,
            CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<WeightHistory> weightHistory;
}
