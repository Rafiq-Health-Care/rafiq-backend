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
@Table(name = "patient_profile", indexes = {@Index(name = "user_idx", columnList = "user_id")})
public class PatientProfile extends BaseEntity {

    private String description;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "patient", cascade = {CascadeType.REMOVE, CascadeType.PERSIST,
            CascadeType.MERGE})
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
