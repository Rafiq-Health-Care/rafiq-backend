package com.nexaworks.rafiq.dto.request.summary;

import java.util.List;

import com.nexaworks.rafiq.entities.MedicineSummary;

public record UpdateConsultationSummaryRequest(String summary, String recoveryPlan,
        List<MedicineSummary> medicineSummary, List<String> requiredLabTest) {
}
