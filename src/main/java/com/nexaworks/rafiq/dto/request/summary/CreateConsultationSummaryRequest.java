package com.nexaworks.rafiq.dto.request.summary;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.MedicineSummary;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for creating a consultation summary")
public record CreateConsultationSummaryRequest(

        @NotNull @Schema(description = "Consultation ID", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID consultationId,

        @Schema(description = "Doctor's summary of the consultation") String summary,

        @Schema(description = "Recommended recovery plan for the patient") String recoveryPlan,

        @Schema(description = "List of prescribed medicines with details") List<MedicineSummary> medicineSummary,

        @Schema(description = "List of required lab tests") List<String> requiredLabTest) {
}
