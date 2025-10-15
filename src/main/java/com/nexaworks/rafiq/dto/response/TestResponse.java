package com.nexaworks.rafiq.dto.response;

import com.nexaworks.rafiq.dto.request.TestResultRequest;

public record TestResponse(TestResultRequest result, String pdf) {
}
