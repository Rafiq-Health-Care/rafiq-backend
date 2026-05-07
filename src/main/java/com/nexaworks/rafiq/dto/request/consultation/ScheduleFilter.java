package com.nexaworks.rafiq.dto.request.consultation;

import java.time.LocalDateTime;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

public record ScheduleFilter(LocalDateTime startDate, LocalDateTime endDate,
        ConsultationStatus status) {
}
