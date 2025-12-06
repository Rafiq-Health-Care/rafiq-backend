package com.nexaworks.rafiq.medication.api.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.context.properties.bind.DefaultValue;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Add reminder request")
public record AddReminderRequest(
        @NotBlank(message = "Medicine component cannot be blank") @Schema UUID medicineId,
        @DefaultValue("true") @Schema boolean vibrate,
        @NotBlank(message = "Next reminder cannot be blank") @Schema LocalDateTime nextReminder) {
}
