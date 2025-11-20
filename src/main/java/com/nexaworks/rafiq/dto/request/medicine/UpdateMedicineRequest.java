package com.nexaworks.rafiq.dto.request.medicine;

import java.time.Instant;

import com.nexaworks.rafiq.enums.MedicineFrequency;
import com.nexaworks.rafiq.enums.MedicineStatus;
import com.nexaworks.rafiq.enums.MedicineType;

import jakarta.validation.constraints.NotNull;

public record UpdateMedicineRequest(@NotNull String name, @NotNull String dosage,
        @NotNull String notes, @NotNull MedicineFrequency frequency, @NotNull Instant startDate,
        @NotNull Instant endDate, @NotNull MedicineType type, @NotNull MedicineStatus status) {
}
