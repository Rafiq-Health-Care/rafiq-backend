package com.nexaworks.rafiq.dto.request.labTest;

import jakarta.validation.constraints.NotBlank;

public record TestRequest(@NotBlank String testName, double result, @NotBlank String unit,
        @NotBlank String status) {
}
