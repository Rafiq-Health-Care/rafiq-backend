package com.nexaworks.rafiq.medication.api.dto.request;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.nexaworks.rafiq.medication.entity.enums.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Update medicine patch request")
public record UpdateMedicinePatchRequest(@Schema Optional<String> name,
        @Schema Optional<String> dosage, @Schema Optional<String> notes,
        @Schema Optional<MedicineFrequency> frequency, @Schema Optional<Instant> startDate,
        @Schema Optional<Instant> endDate, @Schema Optional<MedicineType> type,
        @Schema Optional<MedicineStatus> status,
        @Schema Optional<ReminderFrequency> reminderFrequency,
        @Schema Optional<List<Day>> customDays) {
}
