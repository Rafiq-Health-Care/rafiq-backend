package com.nexaworks.rafiq.medication.api.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.Color;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Add group response")
public record AddGroupResponse(@Schema UUID groupId, @Schema UUID patientId,
        @Schema String description, @Schema Color color, @Schema String name,
        @Schema Instant createdAt, @Schema Instant updatedAt, @Schema String iconUrl,
        @Schema int medicineCount) {
}
