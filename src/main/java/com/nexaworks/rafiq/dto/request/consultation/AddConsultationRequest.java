package com.nexaworks.rafiq.dto.request.consultation;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record AddConsultationRequest(LocalDateTime date, LocalTime startTime,int duration) {
}
