package com.nexaworks.rafiq.medication.api.dto.response;

import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.ReminderFrequency;
import com.nexaworks.rafiq.medication.entity.enums.ReminderStatus;

public record ReminderResponse(UUID id, String time, ReminderFrequency frequency,
        ReminderStatus status) {
}
