package com.nexaworks.rafiq.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerificationRequest(@NotBlank @Email String email, @NotBlank String otp) {
}
