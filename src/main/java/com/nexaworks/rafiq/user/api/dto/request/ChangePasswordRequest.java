package com.nexaworks.rafiq.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Change password request")
public record ChangePasswordRequest(@NotBlank @Schema String accessToken,
        @NotBlank @Size(min = 8, max = 20) @Schema String newPassword) {
}
