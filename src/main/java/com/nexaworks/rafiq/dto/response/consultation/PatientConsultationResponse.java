package com.nexaworks.rafiq.dto.response.consultation;

import java.time.LocalDateTime;
import java.util.UUID;

public record PatientConsultationResponse(UUID id, String doctorName, String doctorBio,
        String doctorImage, LocalDateTime startTime, int duration, UUID summaryId) {
}
