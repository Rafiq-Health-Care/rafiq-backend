package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgetPasswordRequest(@Email @NotBlank String email) {
}
