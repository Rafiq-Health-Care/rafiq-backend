package com.nexaworks.rafiq.dto.response.reminder;

import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.ReminderFrequency;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;

public record ReminderResponse(UUID id, String time, ReminderFrequency frequency,
        ReminderStatus status) {
}
