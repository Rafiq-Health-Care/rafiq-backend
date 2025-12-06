package com.nexaworks.rafiq.user.api.dto.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Doctor registration request")
public record DoctorRegistrationRequest(@Valid @Schema PatientRegistrationRequest user,
        @NotNull @Schema UUID specialization, @NotBlank @Schema String description) {
}
