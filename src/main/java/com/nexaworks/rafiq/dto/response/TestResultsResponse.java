package com.nexaworks.rafiq.dto.response;

import com.nexaworks.rafiq.dto.request.TestRequest;


import java.util.Date;
import java.util.List;
import java.util.UUID;

public record TestResultsResponse(String name,
                                  UUID testId,
                                  String fileUrl,
                                  String fileType,
                                  Date date,
                                  List<TestRequest> tests) {
}
