package com.nexaworks.rafiq.dto.request.summary;

import java.util.List;

import com.nexaworks.rafiq.entities.MedicineSummary;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request body for updating a consultation summary")
public record UpdateConsultationSummaryRequest(

        @Schema(description = "Updated consultation summary") String summary,

        @Schema(description = "Updated recovery plan") String recoveryPlan,

        @Schema(description = "Updated list of prescribed medicines") List<MedicineSummary> medicineSummary,

        @Schema(description = "Updated required lab tests") List<String> requiredLabTest) {
}