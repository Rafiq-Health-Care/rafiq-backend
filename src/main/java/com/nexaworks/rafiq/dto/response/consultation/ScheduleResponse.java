package com.nexaworks.rafiq.dto.response.consultation;

import java.time.LocalDateTime;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

public record ScheduleResponse(UUID id, String patientName, LocalDateTime startTime, int duration,
        ConsultationStatus status) {
}
