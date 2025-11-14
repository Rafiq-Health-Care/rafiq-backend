package com.nexaworks.rafiq.mapper;

import com.nexaworks.rafiq.dto.request.TestRequest;
import com.nexaworks.rafiq.entities.LabResult;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ResultMapper {
  public List<LabResult> toEntity(List<TestRequest> results) {
    if (results == null || results.isEmpty()) {
      return Collections.emptyList();
    }
    return results.stream()
        .map(
            r ->
                LabResult.builder()
                    .name(r.testName())
                    .result(r.result())
                    .unit(r.unit())
                    .status(r.status())
                    .build())
        .collect(Collectors.toList());
  }
}
