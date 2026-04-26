package com.nexaworks.rafiq.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultationChanged(UUID id, LocalDateTime startTime) {
}
