package com.nexaworks.rafiq.labTest.api.dto;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public record TestResultsResponse(String name, UUID testId, String fileUrl, String fileType,
        Date date, List<TestRequest> tests) {
}
