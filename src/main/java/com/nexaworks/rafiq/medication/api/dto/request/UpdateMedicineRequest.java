package com.nexaworks.rafiq.medication.api.dto.request;

import java.time.Instant;
import java.util.List;

import com.nexaworks.rafiq.medication.entity.enums.*;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Update medicine request")
public record UpdateMedicineRequest(@NotNull @Schema String name, @NotNull @Schema String dosage,
        @NotNull @Schema String notes, @NotNull @Schema MedicineFrequency frequency,
        @NotNull @Schema Instant startDate, @NotNull @Schema Instant endDate,
        @NotNull @Schema MedicineType type, @NotNull @Schema MedicineStatus status,
        @NotNull @Schema ReminderFrequency reminderFrequency,
        @NotNull @Schema List<Day> customDays) {
}
