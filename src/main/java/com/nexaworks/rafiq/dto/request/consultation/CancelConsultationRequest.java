package com.nexaworks.rafiq.dto.request.consultation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request body for cancelling a consultation")
public record CancelConsultationRequest(
        @NotBlank @Schema(description = "Reason for cancellation", example = "Patient is unavailable") String reason) {
}