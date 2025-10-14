package com.nexaworks.rafiq.mapper;

import com.nexaworks.rafiq.dto.response.LabResponse;
import com.nexaworks.rafiq.dto.response.PageResponse;
import com.nexaworks.rafiq.entities.Lab;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

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
                all.getNumberOfElements(),
                all.getSize(),
                all.getTotalPages(),
                all.isLast(),
                all.isFirst()
        );
    }
}
