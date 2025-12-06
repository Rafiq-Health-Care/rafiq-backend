package com.nexaworks.rafiq.medication.entity.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.nexaworks.rafiq.shared.entity.BaseEntity;
import com.nexaworks.rafiq.medication.entity.enums.ReminderStatus;

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
public class ReminderLog extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private ReminderStatus status;
    private LocalDateTime timestamp;
    @ManyToOne
    @JoinColumn(name = "reminder_id", referencedColumnName = "id", nullable = false)
    private Reminder reminder;

    private UUID patientId;
}
