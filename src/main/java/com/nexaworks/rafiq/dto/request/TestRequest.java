package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TestRequest(@NotBlank String testName, @NotBlank double result, @NotBlank String unit,
        @NotBlank String status) {
}
