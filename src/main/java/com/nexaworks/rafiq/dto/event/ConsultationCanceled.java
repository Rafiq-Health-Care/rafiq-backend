package com.nexaworks.rafiq.dto.event;

import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

import java.util.UUID;

public record ConsultationCanceled(UUID consultationId, UUID doctorId, UUID patientId, UUID canceledBy, String reason) {
}
