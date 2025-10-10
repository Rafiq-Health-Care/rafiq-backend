package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record DoctorRegistrationRequest(UserRegistrationRequest user,
                                        @NotBlank
                                        UUID specialization,
                                        @NotBlank
                                        String description) {
}
