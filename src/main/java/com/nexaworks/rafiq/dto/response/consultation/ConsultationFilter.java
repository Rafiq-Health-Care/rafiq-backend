package com.nexaworks.rafiq.dto.response.consultation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.Specialization;

public record ConsultationFilter(UUID doctorId, Specialization specialization,
        LocalDateTime fromStartTime, LocalDateTime toStartTime, BigDecimal fromPrice,
        BigDecimal toPrice) {
}
