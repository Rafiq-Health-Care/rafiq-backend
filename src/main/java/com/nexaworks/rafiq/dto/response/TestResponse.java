package com.nexaworks.rafiq.dto.response;

import com.nexaworks.rafiq.dto.request.TestRequest;


import java.util.Date;
import java.util.List;
import java.util.UUID;

public record TestResponse(String name,
                           UUID labId,
                           String labName,
                           Date date,
                           List<TestRequest> tests,
                           UUID testId,
                           String fileUrl,
                           String fileType) {
}
