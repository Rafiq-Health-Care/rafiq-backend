package com.nexaworks.rafiq.dto.response.consultation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.nexaworks.rafiq.dto.response.patientProfile.PatientDto;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record ConsultationSlotResponse(
        @Schema(description = "Unique identifier of the consultation", example = "123e4567-e89b-12d3-a456-426614174000") UUID consultationId,

        @Schema(description = "Unique identifier of the slot", example = "789e1234-e89b-12d3-a456-426614174999") UUID slotId,

        @Schema(description = "Start time of the consultation", example = "2025-06-01T09:00:00") LocalDateTime startTime,

        @Schema(description = "Duration in minutes", example = "30") int durationInMinutes,

        @Schema(description = "Current status of the consultation", example = "BOOKED") ConsultationStatus status,

        @Schema(description = "Price of the consultation", example = "99.99") BigDecimal price,

        @Schema(description = "Patient assigned to this consultation") PatientDto patient,

        @Schema(description = "When the consultation was booked", example = "2025-05-01T08:00:00") LocalDateTime bookedAt,

        @Schema(description = "When the consultation was cancelled, null if not cancelled", example = "2025-05-10T10:00:00") LocalDateTime cancelledAt,

        @Schema(description = "Cancellation reason, null if not cancelled", example = "Patient is unavailable") String reason,

        @Schema(description = "Whether the consultation was cancelled by the patient", example = "true") boolean cancelByPatient) {
}
