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
@ToString(exclude = {"reminderLogs", "patient", "medicine", "group"})
@Table(name = "reminder", indexes = {
        @Index(name = "idx_reminder_patient", columnList = "patient_id"),
        @Index(name = "idx_reminder_medicine", columnList = "medicine_id"),
        @Index(name = "idx_reminder_status", columnList = "status"),
        @Index(name = "idx_reminder_id", columnList = "id")})
public class Reminder extends BaseEntity {

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean vibrate = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderStatus status;

    private LocalDateTime nextReminder;

    @Column(nullable = false, columnDefinition = "boolean default false")
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
