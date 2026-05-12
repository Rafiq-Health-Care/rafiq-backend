package com.nexaworks.rafiq.dto.request.summary;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.MedicineSummary;

import jakarta.validation.constraints.NotNull;

public record CreateConsultationSummaryRequest(@NotNull UUID consultationId, String summary,
        String recoveryPlan, List<MedicineSummary> medicineSummary, List<String> requiredLabTest) {
}
