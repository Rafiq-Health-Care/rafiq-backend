package com.nexaworks.rafiq.mapper;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.request.TestRequest;
import com.nexaworks.rafiq.dto.response.TestResultsResponse;
import com.nexaworks.rafiq.entities.LabTest;

@Component
public class TestMapper {
    public TestResultsResponse mapToTestResponse(LabTest test) {
        return new TestResultsResponse(
                test.getName(),
                test.getId(),
                test.getPdf(),
                test.getFileType(),
                Date.from(test.getDate()),
                test.getLabResults().stream()
                        .map(t -> new TestRequest(t.getName(), t.getResult(), t.getUnit(), t.getStatus()))
                        .toList());
    }
}
