package com.nexaworks.rafiq.dto.request.reminder;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.context.properties.bind.DefaultValue;

import jakarta.validation.constraints.NotBlank;

public record AddReminderRequest(
        @NotBlank(message = "Medicine component cannot be blank") UUID medicineId,
        @DefaultValue("true") boolean vibrate,
        @NotBlank(message = "Next reminder cannot be blank") LocalDateTime nextReminder) {
}
