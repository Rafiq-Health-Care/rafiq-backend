package com.nexaworks.rafiq.dto.response.auth;

import java.util.Optional;

public record LoginResponse(Optional<String> role) {
}
