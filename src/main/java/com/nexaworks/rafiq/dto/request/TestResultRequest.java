package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record TestResultRequest(@NotBlank String name, @NotNull UUID id, Date date, List<TestRequest> tests) {

}
