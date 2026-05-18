package com.nexaworks.rafiq.dto.request.consultation;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for editing a consultation slot")
public record EditConsultationSlotRequest(

        @Schema(description = "Start time of the slot", example = "2025-06-01T09:00:00") LocalDateTime startTime,

        @Schema(description = "End time of the slot", example = "2025-06-01T10:00:00") LocalDateTime endTime,

        @Schema(description = "Duration in minutes", example = "30") int duration) {
}
