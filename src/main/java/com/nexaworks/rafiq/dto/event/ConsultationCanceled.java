package com.nexaworks.rafiq.dto.event;

import java.util.UUID;

public record ConsultationCanceled(UUID consultationId, String doctorEmail, String doctorName,
        String patientEmail, String patientName, boolean cancelByPatient, String reason) {
}
