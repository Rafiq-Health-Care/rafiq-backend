package com.nexaworks.rafiq.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request")
public record LoginRequest(@NotBlank @Schema String email, @NotBlank @Schema String password) {
}
