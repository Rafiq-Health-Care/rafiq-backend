package com.nexaworks.rafiq.mapper;

import com.nexaworks.rafiq.dto.request.TestRequest;
import com.nexaworks.rafiq.dto.request.TestResultRequest;

import com.nexaworks.rafiq.dto.response.TestResponse;
import com.nexaworks.rafiq.entities.LabTest;
import org.springframework.stereotype.Component;


import java.util.Date;


@Component
public class TestMapper {
    public TestResponse mapToTestResponse(LabTest test) {
        return new TestResponse(
                test.getName(),
                test.getLab().getId(),
                test.getLab().getName(),
                Date.from(test.getDate()),
                test.getLabResults().stream()
                        .map(labResult -> new TestRequest(
                                labResult.getName(),
                                labResult.getResult(),
                                labResult.getUnit(),
                                labResult.getStatus()
                        ))
                        .toList(),
                test.getId(),
                test.getPdf(),
                test.getFileType()
        );
    }
}
