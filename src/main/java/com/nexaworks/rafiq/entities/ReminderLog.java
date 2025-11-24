package com.nexaworks.rafiq.entities;

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
public class ReminderLog extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private ReminderStatus status;
    private String message;
    @ManyToOne
    @JoinColumn(name = "reminder_id", referencedColumnName = "id", nullable = false)
    private Reminder reminder;
}
