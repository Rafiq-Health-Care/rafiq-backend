package com.nexaworks.rafiq.dto.response.consultation;

import com.nexaworks.rafiq.entities.enums.Specialization;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultationFilter(UUID doctorId,
                                 Specialization specialization,
                                 LocalDateTime fromStartTime,
                                 LocalDateTime toStartTime,
                                 BigDecimal fromPrice,
                                 BigDecimal toPrice) {
}
