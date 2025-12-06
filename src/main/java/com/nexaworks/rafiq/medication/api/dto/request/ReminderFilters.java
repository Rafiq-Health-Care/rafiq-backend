package com.nexaworks.rafiq.medication.api.dto.request;

import java.time.Instant;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.ReminderStatus;

public record ReminderFilters(Instant startDate, Instant endDate, UUID medicineId,
        ReminderStatus status) {
}
