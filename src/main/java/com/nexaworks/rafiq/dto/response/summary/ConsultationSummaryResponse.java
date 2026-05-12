package com.nexaworks.rafiq.dto.response.summary;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.MedicineSummary;

public record ConsultationSummaryResponse(UUID id, UUID consultationId, UUID doctorId,
        UUID patientId, String summary, String recoveryPlan, List<MedicineSummary> medicineSummary,
        List<String> requiredLabTest) {
}
