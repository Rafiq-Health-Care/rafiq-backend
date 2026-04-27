package com.nexaworks.rafiq.dto.event;

public record ConsultationAddedEvent(java.util.UUID consultationId, java.util.UUID doctorId, java.time.LocalDateTime date) {
}
