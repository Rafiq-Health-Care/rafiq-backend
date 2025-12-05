package com.nexaworks.rafiq.user.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ResetPasswordRequest(@NotBlank @NotEmpty String oldPassword,
        @NotBlank @NotEmpty String newPassword) {
}
