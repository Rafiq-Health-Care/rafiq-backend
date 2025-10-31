package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record ResetPasswordRequest(@NotBlank
                                   @NotEmpty
                                   String oldPassword,
                                   @NotBlank
                                   @NotEmpty
                                   String newPassword) {
}
