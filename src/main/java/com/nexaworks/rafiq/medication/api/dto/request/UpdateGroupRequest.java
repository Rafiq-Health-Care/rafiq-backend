package com.nexaworks.rafiq.medication.api.dto.request;

import com.nexaworks.rafiq.medication.entity.enums.Color;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Update group request")
public record UpdateGroupRequest(@Size(max = 50) @Schema String name,
        @Size(max = 100) @Schema String description, @Schema Color color) {
}
