package com.nexaworks.rafiq.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "Reset password request")
public record ResetPasswordRequest(@NotBlank @NotEmpty @Schema String oldPassword,
        @NotBlank @NotEmpty @Schema String newPassword) {
}
