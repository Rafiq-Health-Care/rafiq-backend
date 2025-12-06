package com.nexaworks.rafiq.medication.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Add response")
public record AddResponse<T>(@Schema boolean success, @Schema String message, @Schema T data) {
}
