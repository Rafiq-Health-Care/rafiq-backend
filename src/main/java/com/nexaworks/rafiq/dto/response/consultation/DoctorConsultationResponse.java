package com.nexaworks.rafiq.dto.response.consultation;

import java.time.LocalDateTime;
import java.util.UUID;

public record DoctorConsultationResponse(UUID id, LocalDateTime startTime, LocalDateTime endTime) {
}
