package com.nexaworks.rafiq.dto.event;

import java.time.LocalDate;

public record ConsultationAddedEvent(java.util.UUID consultationId, java.util.UUID doctorId, LocalDate date) {
}
