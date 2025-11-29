package com.nexaworks.rafiq.dto.request.medicine;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.Day;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.MedicineType;
import com.nexaworks.rafiq.entities.enums.ReminderFrequency;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMedicineRequest(@NotNull UUID medicineId, @NotBlank String dosage,
        @NotNull MedicineFrequency frequency, @NotNull ReminderFrequency reminderFrequency,
        List<Day> customDays, @NotNull Instant startDate, Instant endDate, String notes,
        MedicineType type) {
}
