package com.nexaworks.rafiq.dto.request.reminder;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record AddReminderRequest(
        @NotBlank(message = "Medicine component cannot be blank") UUID medicineId,
        Instant startDate, Instant endDate, boolean vibrate) {
}
