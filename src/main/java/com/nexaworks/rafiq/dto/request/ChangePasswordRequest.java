package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        String accessToken,
        @NotBlank
        @Size(min = 8,max = 20)
        String newPassword) {
}
