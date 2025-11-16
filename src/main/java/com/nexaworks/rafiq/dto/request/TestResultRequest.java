package com.nexaworks.rafiq.dto.request;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public record TestResultRequest(@NotBlank String name, Date date, @Valid List<TestRequest> tests,
        UUID testId) {
}
