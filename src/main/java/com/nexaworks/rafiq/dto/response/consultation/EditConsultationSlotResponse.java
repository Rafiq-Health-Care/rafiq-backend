package com.nexaworks.rafiq.dto.response.consultation;

import java.time.LocalDateTime;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.SlotStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response body after editing a consultation slot")
public record EditConsultationSlotResponse(

        @Schema(description = "Unique identifier of the slot", example = "123e4567-e89b-12d3-a456-426614174000") UUID slotId,

        @Schema(description = "Start time of the slot", example = "2025-06-01T09:00:00") LocalDateTime startTime,

        @Schema(description = "End time of the slot", example = "2025-06-01T10:00:00") LocalDateTime endTime,

        @Schema(description = "Duration in minutes", example = "30") int duration,

        @Schema(description = "Current status of the slot", example = "AVAILABLE") SlotStatus status,

        @Schema(description = "When the slot was created", example = "2025-05-01T08:00:00") LocalDateTime createdAt,

        @Schema(description = "When the slot was last updated", example = "2025-05-15T10:30:00") LocalDateTime updatedAt) {
}
