package com.nexaworks.rafiq.dto.request.doctor;

import jakarta.validation.constraints.NotBlank;

public record EditBiographyRequest(@NotBlank String biography) {
}
