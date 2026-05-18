package com.nexaworks.rafiq.dto.request.consultation;

import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.PaymentProvider;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body for reserving a consultation slot")
public record ReserveConsultationRequest(

        @NotNull @Schema(description = "UUID of the slot to reserve", example = "123e4567-e89b-12d3-a456-426614174000") UUID slotId,

        @Schema(description = "Optional notes for the doctor", example = "I have a headache for 3 days") String notes,

        @NotNull @Schema(description = "Payment provider to process the reservation", example = "STRIPE") PaymentProvider provider) {
}
