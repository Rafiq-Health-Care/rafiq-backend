package com.nexaworks.rafiq.dto.event;

import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.SlotStatus;

public record ConsultationCancelled(UUID id, SlotStatus status) {
}
