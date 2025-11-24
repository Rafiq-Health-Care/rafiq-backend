package com.nexaworks.rafiq.dto.request.medicine;

import java.time.Instant;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.MedicineType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMedicineRequest(@NotNull UUID medicineId, @NotBlank String dosage,
        @NotNull MedicineFrequency frequency, @NotNull Instant startDate, Instant endDate,
        String notes, MedicineType type) {
}
