package com.nexaworks.rafiq.dto.response.summary;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.MedicineSummary;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response containing consultation summary details")
public record ConsultationSummaryResponse(

        @Schema(description = "Consultation summary ID") UUID id,

        @Schema(description = "Related consultation ID") UUID consultationId,

        @Schema(description = "Doctor ID who created the summary") UUID doctorId,

        @Schema(description = "Patient ID") UUID patientId,

        @Schema(description = "Medical summary") String summary,

        @Schema(description = "Recovery plan") String recoveryPlan,

        @Schema(description = "List of prescribed medicines") List<MedicineSummary> medicineSummary,

        @Schema(description = "Required lab tests") List<String> requiredLabTest) {
}