package com.nexaworks.rafiq.dto.response.consultation;

import java.time.LocalDateTime;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.SlotStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Doctor's schedule entry for a consultation slot")
public record ScheduleResponse(

        @Schema(description = "Unique identifier of the slot", example = "123e4567-e89b-12d3-a456-426614174000") UUID slotId,

        @Schema(description = "Full name of the booked patient, null if slot is unbooked", example = "John Doe") String patientName,

        @Schema(description = "Start time of the slot", example = "2025-06-01T09:00:00") LocalDateTime startTime,

        @Schema(description = "Duration in minutes", example = "30") int durationInMinutes,

        @Schema(description = "Current status of the slot", example = "AVAILABLE") SlotStatus status,

        @Schema(description = "Consultation ID linked to this slot, null if unbooked", example = "789e1234-e89b-12d3-a456-426614174999") UUID consultationId) {
}
