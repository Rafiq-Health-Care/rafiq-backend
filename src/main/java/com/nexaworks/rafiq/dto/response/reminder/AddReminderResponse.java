package com.nexaworks.rafiq.dto.response.reminder;

import java.time.Instant;
import java.util.UUID;

public record AddReminderResponse(UUID id, UUID medicineId, boolean vibrate, Instant createdAt,
        Instant updatedAt) {
}
