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
        return new TestResponse(new TestResultRequest(test.getName(),
                test.getId(),Date.from(test.getDate()),
                test.getLabResults().stream().map(
                        t -> new TestRequest(t.getName(),
                                t.getResult(),
                                t.getUnit(),
                                t.getStatus())
                ).toList(),test.getId()),test.getPdf());
    }
}
