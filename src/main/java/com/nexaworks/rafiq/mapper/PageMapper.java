package com.nexaworks.rafiq.mapper;

import com.nexaworks.rafiq.dto.response.LabResponse;
import com.nexaworks.rafiq.dto.response.PageResponse;
import com.nexaworks.rafiq.dto.response.TestResponse;
import com.nexaworks.rafiq.entities.Lab;
import com.nexaworks.rafiq.entities.LabTest;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {
    public PageResponse<LabResponse> mapToLabPage(Page<Lab> all) {
        List<LabResponse> content = all.getContent().stream()
                .map(lab -> new LabResponse(lab.getId(), lab.getName(), lab.getLogo()))
                .collect(Collectors.toList());
        return new PageResponse<>(
                content, (int) all.getTotalElements(), all.getSize(), all.getTotalPages(), all.isLast(), all.isFirst());
    }

    public PageResponse<TestResponse> mapToTestResponse(Page<LabTest> all) {
        List<TestResponse> content = all.getContent().stream()
                .map(labTest ->
                        new TestResponse(labTest.getName(), labTest.getId(), labTest.getPdf(), labTest.getFileType()))
                .collect(Collectors.toList());
        return new PageResponse<>(
                content, (int) all.getTotalElements(), all.getSize(), all.getTotalPages(), all.isLast(), all.isFirst());
    }
}
