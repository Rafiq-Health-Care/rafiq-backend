package com.nexaworks.rafiq.dto.request.medicine;

import java.time.Instant;
import java.util.List;

import com.nexaworks.rafiq.entities.enums.*;

import jakarta.validation.constraints.NotNull;

public record UpdateMedicineRequest(@NotNull String name, @NotNull String dosage,
        @NotNull String notes, @NotNull MedicineFrequency frequency, @NotNull Instant startDate,
        @NotNull Instant endDate, @NotNull MedicineType type, @NotNull MedicineStatus status,
        @NotNull ReminderFrequency reminderFrequency, @NotNull List<Day> customDays) {
}
