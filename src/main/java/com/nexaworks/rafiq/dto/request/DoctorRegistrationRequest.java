package com.nexaworks.rafiq.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DoctorRegistrationRequest(@Valid UserRegistrationRequest user,
                                        @NotNull
                                        UUID specialization,
                                        @NotBlank
                                        String description) {
}
