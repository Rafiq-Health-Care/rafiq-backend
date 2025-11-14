package com.nexaworks.rafiq.entities;

import java.util.List;

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
public class PatientProfile extends BaseEntity {

    private String description;

    @OneToOne(mappedBy = "patientProfile")
    private User user;

    @OneToMany(mappedBy = "patient")
    private List<LabTest> labTests;
}
