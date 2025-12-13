package com.nexaworks.rafiq.medication.api.dto.response;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Add reminder response")
public record AddReminderResponse(@Schema UUID id, @Schema UUID medicineId, @Schema boolean vibrate,
        @Schema Instant createdAt, @Schema Instant updatedAt) {
}
