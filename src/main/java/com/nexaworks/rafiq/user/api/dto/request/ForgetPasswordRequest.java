package com.nexaworks.rafiq.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Forget password request")
public record ForgetPasswordRequest(@Email @NotBlank @Schema String email) {
}
