package com.nexaworks.rafiq.dto.response.auth;

import java.util.UUID;

public record LoginResponse(String role, UUID userId) {
}
