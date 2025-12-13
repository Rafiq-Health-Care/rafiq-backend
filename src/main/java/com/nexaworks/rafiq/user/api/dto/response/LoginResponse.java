package com.nexaworks.rafiq.user.api.dto.response;

import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response")
public record LoginResponse(@Schema Optional<String> role) {
}
