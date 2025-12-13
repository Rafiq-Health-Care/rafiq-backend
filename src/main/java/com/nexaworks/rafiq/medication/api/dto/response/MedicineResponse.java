package com.nexaworks.rafiq.medication.api.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.*;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Medicine response")
public record MedicineResponse(@Schema UUID id, @Schema UUID patientId, @Schema String name,
        @Schema String dosage, @Schema MedicineFrequency frequency,
        @Schema ReminderFrequency reminderFrequency, @Schema List<Day> customDays,
        @Schema Instant startDate, @Schema Instant endDate, @Schema String notes,
        @Schema String photoUrl, @Schema MedicineType type, @Schema MedicineStatus status,
        @Schema UUID groupId, @Schema String groupName, @Schema UUID reminderId,
        @Schema LocalDateTime nextReminder, @Schema Instant createdAt, @Schema Instant updatedAt) {
}
