package com.nexaworks.rafiq.dto.response;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.dto.request.labTest.TestRequest;

public record TestResultsResponse(String name, UUID testId, String fileUrl, String fileType,
        Date date, List<TestRequest> tests) {
}
