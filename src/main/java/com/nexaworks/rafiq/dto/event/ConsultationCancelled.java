package com.nexaworks.rafiq.dto.event;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

import java.util.UUID;

public record ConsultationCancelled(UUID id, ConsultationStatus status) {
}
