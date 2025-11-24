package com.nexaworks.rafiq.entities;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import com.nexaworks.rafiq.entities.enums.Day;
import com.nexaworks.rafiq.entities.enums.ReminderFrequency;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;

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
    // todo review this
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "custom_days", joinColumns = @JoinColumn(name = "reminder_id"))
    @Column(name = "day")
    @Enumerated(EnumType.STRING)
    private List<Day> customDays;
    private boolean vibrate;
    private Instant startDate;
    private Instant endDate;
    @Enumerated(EnumType.STRING)
    private ReminderStatus status;
    private LocalDateTime nextReminder;
    @ManyToOne
    @JoinColumn(name = "medicine_id", referencedColumnName = "id", nullable = false)
    private Medicine medicine;
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private PatientProfile patient;
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;
    @OneToMany(mappedBy = "reminder")
    private List<ReminderLog> reminderLogs;

}
