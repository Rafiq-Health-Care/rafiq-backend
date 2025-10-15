package com.nexaworks.rafiq.mapper;

import com.nexaworks.rafiq.dto.request.TestRequest;
import com.nexaworks.rafiq.dto.request.TestResultRequest;
import com.nexaworks.rafiq.dto.response.LabResponse;
import com.nexaworks.rafiq.dto.response.PageResponse;
import com.nexaworks.rafiq.entities.Lab;
import com.nexaworks.rafiq.entities.LabResult;
import com.nexaworks.rafiq.entities.LabTest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PageMapper {
    public PageResponse<LabResponse> mapToLabPage(Page<Lab> all) {
        List<LabResponse> content = all.getContent().stream()
                .map(lab -> new LabResponse(lab.getId(), lab.getName(), lab.getLogo()))
                .collect(Collectors.toList());
        return new PageResponse<>(
                content,
                (int) all.getTotalElements(),
                all.getSize(),
                all.getTotalPages(),
                all.isLast(),
                all.isFirst()
        );
    }

    public PageResponse<TestResultRequest> mapToTestResponse(Page<LabTest> all) {
        List<TestResultRequest> content = all.getContent().stream()
                .map(labTest -> {
                    List<TestRequest> tests = (labTest.getLabResults() == null ? List.<LabResult>of() : labTest.getLabResults())
                            .stream()
                            .map(lr -> new TestRequest(
                                    lr.getName(),
                                    lr.getResult(),
                                    lr.getUnit(),
                                    lr.getStatus()
                            ))
                            .collect(Collectors.toList());

                    Instant instant = labTest.getDate();
                    Date date = instant != null ? Date.from(instant) : null;

                    return new TestResultRequest(
                            labTest.getName(),
                            labTest.getId(),
                            date,
                            tests,labTest.getId()
                    );
                })
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                (int) all.getTotalElements(),
                all.getSize(),
                all.getTotalPages(),
                all.isLast(),
                all.isFirst()
        );
    }
}
