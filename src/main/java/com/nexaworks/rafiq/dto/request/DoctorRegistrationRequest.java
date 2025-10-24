package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DoctorRegistrationRequest(UserRegistrationRequest user,
                                        @NotNull
                                        UUID specialization,
                                        @NotBlank
                                        String description) {
}
