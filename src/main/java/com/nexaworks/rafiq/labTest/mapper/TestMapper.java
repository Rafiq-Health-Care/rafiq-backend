package com.nexaworks.rafiq.labTest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.labTest.api.dto.TestResponse;
import com.nexaworks.rafiq.labTest.api.dto.TestResultsResponse;
import com.nexaworks.rafiq.labTest.entity.LabTest;

@Mapper(componentModel = "spring")
public interface TestMapper {
    @Mapping(source = "id", target = "testId")
    @Mapping(source = "labResults", target = "tests")
    TestResultsResponse mapToTestResponse(LabTest test);
    @Mapping(source = "id", target = "testId")
    @Mapping(source = "pdf", target = "fileUrl")
    TestResponse toResponse(LabTest test);
}
