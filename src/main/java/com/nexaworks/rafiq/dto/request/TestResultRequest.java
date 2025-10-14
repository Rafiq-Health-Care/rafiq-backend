package com.nexaworks.rafiq.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Date;
import java.util.List;

public record TestResultRequest(@NotBlank String name, String labName, Date date, List<TestRequest> results) {

}
