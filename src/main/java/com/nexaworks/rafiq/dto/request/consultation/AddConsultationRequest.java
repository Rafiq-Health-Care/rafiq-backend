package com.nexaworks.rafiq.dto.request.consultation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record AddConsultationRequest(
        @NotNull LocalDate date,
        @NotNull LocalTime startTime,
        int duration,
        @NotNull @DecimalMin("0.0") BigDecimal price
) {
}
