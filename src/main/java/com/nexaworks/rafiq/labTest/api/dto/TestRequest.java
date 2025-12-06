package com.nexaworks.rafiq.labTest.api.dto;

import jakarta.validation.constraints.NotBlank;

public record TestRequest(@NotBlank String testName, double result, @NotBlank String unit,
        @NotBlank String status) {
}
