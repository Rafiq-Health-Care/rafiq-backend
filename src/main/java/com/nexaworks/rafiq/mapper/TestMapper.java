package com.nexaworks.rafiq.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.response.labTest.TestResultsResponse;
import com.nexaworks.rafiq.entities.LabTest;

@Mapper(componentModel = "spring")
public interface TestMapper {
    @Mapping(source = "id", target = "testId")
    @Mapping(source = "labResults", target = "tests")
    TestResultsResponse mapToTestResponse(LabTest test);
}
