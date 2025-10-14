package com.nexaworks.rafiq.dto.request;

import com.nexaworks.rafiq.enums.Status;
import jakarta.validation.constraints.NotBlank;

public record TestRequest(@NotBlank String testName,
                          @NotBlank double result,
                          @NotBlank String unit,
                          @NotBlank Status status) {
}
