package com.nexaworks.rafiq.dto.request.user;

import com.nexaworks.rafiq.entities.enums.Specialization;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DoctorRegistrationRequest(@Valid UserRegistrationRequest user,
        @NotNull Specialization specialization, @NotBlank String description) {
}
