package com.nexaworks.rafiq.dto.response.consultation;

import java.util.Map;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.EventType;

public record ConsultationEvent(UUID id, EventType type, Map<String, Object> data) {
}
