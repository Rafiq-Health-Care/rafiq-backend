package com.nexaworks.rafiq.entities;

import java.util.List;

import com.nexaworks.rafiq.enums.Day;
import com.nexaworks.rafiq.enums.ReminderFrequency;

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
public class Reminder extends BaseEntity {

    private int hour;
    private int minute;
    @Enumerated(EnumType.STRING)
    private ReminderFrequency frequency;
    private List<Day> customDays;
    private boolean vibrate;
    @ManyToOne
    @JoinColumn(name = "medicine_id", referencedColumnName = "id", nullable = false)
    private Medicine medicine;
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private PatientProfile patient;
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id", nullable = true)
    private Group group;
    @OneToMany(mappedBy = "reminder")
    private List<ReminderLog> reminderLogs;

}
