package com.nexaworks.rafiq.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Email verification request")
public record VerificationRequest(@NotBlank @Email @Schema String email,
        @NotBlank @Schema String otp) {
}
