package com.nexaworks.rafiq.user.api.dto.request;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DoctorRegistrationRequest(@Valid PatientRegistrationRequest user,
        @NotNull UUID specialization, @NotBlank String description) {
}
