package com.nexaworks.rafiq.labTest.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.labTest.entity.LabResult;
import com.nexaworks.rafiq.shared.dto.TestRequest;

@Mapper(componentModel = "spring")
public interface ResultMapper {
    @Mapping(source = "testName", target = "name")
    LabResult toEntity(TestRequest request);

    List<LabResult> toEntity(List<TestRequest> results);

    @Mapping(source = "name", target = "testName")
    TestRequest toDto(LabResult labResult);

    List<TestRequest> toDto(List<LabResult> labResults);
}
