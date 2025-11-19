package com.nexaworks.rafiq.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.dto.request.TestRequest;
import com.nexaworks.rafiq.entities.LabResult;

@Mapper(componentModel = "spring")
public interface ResultMapper {
    @Mapping(source = "testName", target = "name")
    LabResult toEntity(TestRequest request);

    List<LabResult> toEntity(List<TestRequest> results);
}
