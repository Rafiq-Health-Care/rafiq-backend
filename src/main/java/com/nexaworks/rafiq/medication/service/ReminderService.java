package com.nexaworks.rafiq.medication.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.medication.api.dto.request.GetAllRemindersHistoryResponseProjection;
import com.nexaworks.rafiq.medication.api.dto.request.ReminderFilters;
import com.nexaworks.rafiq.medication.api.dto.response.GetAllRemindersResponse;
import com.nexaworks.rafiq.medication.entity.enums.ReminderStatus;
import com.nexaworks.rafiq.medication.entity.model.Reminder;

public interface ReminderService {
    Reminder createReminder(Reminder reminder, UUID patientId) throws SchedulerException;

    Page<GetAllRemindersHistoryResponseProjection> getHistory(Pageable pageable,
            ReminderFilters filters, UUID patientId);

    void updateReminderStatus(UUID reminderId, ReminderStatus status, LocalDateTime takenTime,
            UUID patientId);

    Page<GetAllRemindersResponse> getAllReminders(Pageable pageable, UUID patientId);

    Reminder getReminderById(UUID reminderId, UUID patientId);

    Reminder updateVibration(UUID reminderId, Boolean vibrate, UUID patientId);

    void deleteReminder(UUID reminderId, UUID patientId);

    void disableReminder(UUID reminderId, Boolean disable, UUID patientId);
}
