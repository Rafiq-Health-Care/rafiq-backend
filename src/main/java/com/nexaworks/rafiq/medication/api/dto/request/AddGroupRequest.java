package com.nexaworks.rafiq.medication.api.dto.request;

import com.nexaworks.rafiq.medication.entity.enums.Color;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Add group request")
public record AddGroupRequest(@NotNull @Schema String name, @Schema String description,
        @Schema Color color) {

}
