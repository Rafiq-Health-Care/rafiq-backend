package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@NotBlank String refreshToken, @NotBlank String jwtToken) {
}
