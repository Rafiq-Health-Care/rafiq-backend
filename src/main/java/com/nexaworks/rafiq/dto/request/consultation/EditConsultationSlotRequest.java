package com.nexaworks.rafiq.dto.request.consultation;

import java.time.LocalDateTime;

import com.nexaworks.rafiq.entities.enums.SlotStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for editing a consultation slot")
public record EditConsultationSlotRequest(

        @NotNull @Schema(description = "Start time of the slot", example = "2025-06-01T09:00:00") LocalDateTime startTime,

        @NotNull @Schema(description = "End time of the slot", example = "2025-06-01T10:00:00") LocalDateTime endTime,

        @NotNull @Schema(description = "Duration in minutes", example = "30") int duration,
        @NotNull @Schema(description = "Status of the slot", example = "AVAILABLE") SlotStatus status) {
}
