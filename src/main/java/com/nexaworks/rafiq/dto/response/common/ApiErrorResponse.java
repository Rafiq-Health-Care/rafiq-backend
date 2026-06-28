package com.nexaworks.rafiq.dto.response.common;

import java.time.LocalDateTime;

public record ApiErrorResponse(
    int status,
    String message,
    LocalDateTime timestamp
) {
}
