package com.nexaworks.rafiq.entities;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.BatchSize;

import com.nexaworks.rafiq.entities.enums.ReminderStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "reminder", indexes = {@Index(name = "patient_idx", columnList = "patient_id"),
        @Index(name = "medicine_idx", columnList = "medicine_id")})
public class Reminder extends BaseEntity {

    private boolean vibrate;
    @Enumerated(EnumType.STRING)
    private ReminderStatus status;
    private LocalDateTime nextReminder;
    @Builder.Default
    private Boolean disable = false;
    @OneToOne
    @JoinColumn(name = "medicine_id", referencedColumnName = "id", nullable = false)
    private Medicine medicine;
    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false)
    private Patient patient;
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;
    @OneToMany(mappedBy = "reminder", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    private List<ReminderLog> reminderLogs;

}
