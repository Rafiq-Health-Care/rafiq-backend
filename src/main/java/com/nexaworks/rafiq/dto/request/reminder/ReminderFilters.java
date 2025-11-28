package com.nexaworks.rafiq.dto.request.reminder;

import java.time.Instant;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.ReminderStatus;

public record ReminderFilters(Instant startDate, Instant endDate, UUID medicineId,
        ReminderStatus status) {
}
