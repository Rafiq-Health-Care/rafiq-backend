package com.nexaworks.rafiq.dto.response.consultation;

import com.nexaworks.rafiq.entities.enums.EventType;

import java.util.Map;
import java.util.UUID;

public record ConsultationEvent(UUID id, EventType type, Map<String,Object> data) {
}
