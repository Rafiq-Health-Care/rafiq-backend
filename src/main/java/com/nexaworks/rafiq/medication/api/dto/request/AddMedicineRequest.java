package com.nexaworks.rafiq.medication.api.dto.request;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.Day;
import com.nexaworks.rafiq.medication.entity.enums.MedicineFrequency;
import com.nexaworks.rafiq.medication.entity.enums.MedicineType;
import com.nexaworks.rafiq.medication.entity.enums.ReminderFrequency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Add medicine request")
public record AddMedicineRequest(@NotNull @Schema UUID medicineId, @NotBlank @Schema String dosage,
        @NotNull @Schema MedicineFrequency frequency,
        @NotNull @Schema ReminderFrequency reminderFrequency, @Schema List<Day> customDays,
        @NotNull @Schema Instant startDate, @Schema Instant endDate, @Schema String notes,
        @Schema MedicineType type) {
}
