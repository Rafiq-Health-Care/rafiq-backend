package com.nexaworks.rafiq.entities;

import java.time.LocalDateTime;

import com.nexaworks.rafiq.entities.enums.ReminderStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString(exclude = {"reminder", "patient"})
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "reminder_log", indexes = {@Index(name = "reminder_idx", columnList = "reminder_id")})
public class ReminderLog extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private ReminderStatus status;
    private LocalDateTime timestamp;
    @ManyToOne
    @JoinColumn(name = "reminder_id", referencedColumnName = "id", nullable = false)
    private Reminder reminder;
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;
}
