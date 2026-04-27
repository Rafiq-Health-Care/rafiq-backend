package com.nexaworks.rafiq.dto.request.consultation;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

import java.time.LocalDateTime;

public record ScheduleFilter(LocalDateTime startDate, LocalDateTime endDate, ConsultationStatus status) {
}
