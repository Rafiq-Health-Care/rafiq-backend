package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public record TestResultRequest(
    @NotBlank String name, Date date, List<TestRequest> tests, UUID testId) {}
