package com.nexaworks.rafiq.user.api.dto.response;

import java.util.Optional;

public record LoginResponse(Optional<String> role) {
}
