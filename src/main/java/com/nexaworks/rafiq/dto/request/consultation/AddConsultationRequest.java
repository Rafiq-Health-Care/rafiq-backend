package com.nexaworks.rafiq.dto.request.consultation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AddConsultationRequest(
        @NotNull LocalDateTime startTime,
        int duration,
        @NotNull @DecimalMin("0.0") BigDecimal price
) {
}
