package com.nexaworks.rafiq.medication.entity.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.shared.entity.BaseEntity;
import org.hibernate.annotations.BatchSize;

import com.nexaworks.rafiq.medication.entity.enums.ReminderStatus;

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
    private UUID patientId;
    @ManyToOne
    @JoinColumn(name = "group_id", referencedColumnName = "id")
    private Group group;
    @OneToMany(mappedBy = "reminder", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 100)
    private List<ReminderLog> reminderLogs;

}
