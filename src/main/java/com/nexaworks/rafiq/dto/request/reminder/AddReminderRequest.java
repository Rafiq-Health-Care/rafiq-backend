package com.nexaworks.rafiq.dto.request.reminder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.Day;
import com.nexaworks.rafiq.entities.enums.ReminderFrequency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AddReminderRequest(
        @NotBlank(message = "Medicine component cannot be blank") UUID medicineId,
        @NotBlank @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d)$", message = "time must be in HH:mm format") String time,
        @NotBlank(message = "Frequency cannot be blank") ReminderFrequency frequency,
        List<Day> dayOfWeek, Instant startDate, Instant endDate, boolean vibrate) {
}
