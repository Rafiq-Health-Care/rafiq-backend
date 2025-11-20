package com.nexaworks.rafiq.dto.response;

import com.nexaworks.rafiq.dto.request.labTest.TestResultRequest;

public record TestResultResponse(TestResultRequest result, String testFile, String fileType) {
}
