package com.nexaworks.rafiq.dto.response.consultation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after reserving a consultation slot")
public record ReserveConsultationResponse(
        @Schema(description = "Payment key to complete the reservation", example = "pay_abc123xyz") String paymentKey) {
}