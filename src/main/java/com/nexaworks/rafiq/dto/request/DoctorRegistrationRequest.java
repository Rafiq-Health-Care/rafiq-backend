package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DoctorRegistrationRequest(UserRegistrationRequest user,
                                        @NotBlank
                                        String specialization,
                                        @NotBlank
                                        String description) {
}
