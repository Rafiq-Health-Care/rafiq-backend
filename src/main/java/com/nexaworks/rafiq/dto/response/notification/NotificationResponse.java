package com.nexaworks.rafiq.dto.response.notification;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record NotificationResponse(UUID id, String title, String message, boolean read,
        LocalDateTime createdAt, Map<String, String> data) {
}
