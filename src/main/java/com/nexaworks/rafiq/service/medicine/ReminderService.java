package com.nexaworks.rafiq.service.medicine;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.request.reminder.AddReminderRequest;
import com.nexaworks.rafiq.dto.request.reminder.GetAllRemindersHistoryResponseProjection;
import com.nexaworks.rafiq.dto.request.reminder.ReminderFilters;
import com.nexaworks.rafiq.dto.response.reminder.AddReminderResponse;
import com.nexaworks.rafiq.dto.response.reminder.GetAllRemindersResponse;
import com.nexaworks.rafiq.dto.response.reminder.GetReminderByIdResponse;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;

public interface ReminderService {
    AddReminderResponse createReminder(AddReminderRequest request);

    Page<GetAllRemindersHistoryResponseProjection> getHistory(Pageable pageable,
            ReminderFilters filters);

    void updateReminderStatus(UUID reminderId, ReminderStatus status, LocalDateTime takenTime);

    Page<GetAllRemindersResponse> getAllReminders(Pageable pageable);

    GetReminderByIdResponse getReminderById(UUID reminderId);

    AddReminderResponse updateVibration(UUID reminderId, Boolean vibrate);

    void deleteReminder(UUID reminderId);

    void disableReminder(UUID reminderId, Boolean disable);
}
