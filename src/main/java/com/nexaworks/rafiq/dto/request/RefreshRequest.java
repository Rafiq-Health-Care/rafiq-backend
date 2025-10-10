package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank
                             String refreshToken) {
}
