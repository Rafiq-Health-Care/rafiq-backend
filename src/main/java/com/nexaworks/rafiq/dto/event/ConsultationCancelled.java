package com.nexaworks.rafiq.dto.event;

import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

public record ConsultationCancelled(UUID id, ConsultationStatus status) {
}
