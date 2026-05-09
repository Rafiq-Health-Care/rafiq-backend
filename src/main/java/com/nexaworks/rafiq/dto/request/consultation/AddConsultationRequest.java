package com.nexaworks.rafiq.dto.request.consultation;

import java.time.LocalDateTime;

import com.nexaworks.rafiq.validation.annotation.FutureDate;

import jakarta.validation.constraints.NotNull;

public record AddConsultationRequest(@NotNull @FutureDate LocalDateTime startTime, int duration) {
}
