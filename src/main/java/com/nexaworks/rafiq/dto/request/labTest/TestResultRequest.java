package com.nexaworks.rafiq.dto.request.labTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record TestResultRequest(@NotBlank String name, LocalDateTime date,
        @Valid List<TestRequest> tests, UUID testId) {
}
