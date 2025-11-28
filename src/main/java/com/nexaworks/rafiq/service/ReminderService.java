package com.nexaworks.rafiq.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.request.reminder.GetAllRemindersHistoryResponseProjection;
import com.nexaworks.rafiq.dto.request.reminder.ReminderFilters;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;

public interface ReminderService {
    Reminder createReminder(Reminder reminder) throws SchedulerException;

    Page<GetAllRemindersHistoryResponseProjection> getHistory(Pageable pageable,
            ReminderFilters filters);

    void updateReminderStatus(UUID reminderId, ReminderStatus status, LocalDateTime takenTime);
}
