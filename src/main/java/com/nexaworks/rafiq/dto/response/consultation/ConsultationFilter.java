package com.nexaworks.rafiq.dto.response.consultation;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.Specialization;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ConsultationFilter(UUID doctorId,
                                 Specialization specialization,
                                 LocalDateTime startTime,
                                 BigDecimal price) {
}
