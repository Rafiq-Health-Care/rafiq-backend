package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.ReminderLog;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, UUID> {
    ReminderLog findReminderLogByReminder_IdAndStatus(UUID reminderId, ReminderStatus status);
}
