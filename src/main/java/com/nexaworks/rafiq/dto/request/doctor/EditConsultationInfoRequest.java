package com.nexaworks.rafiq.dto.request.doctor;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record EditConsultationInfoRequest(@NotNull @Positive BigDecimal price) {
}
