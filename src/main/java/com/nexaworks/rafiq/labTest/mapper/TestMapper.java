package com.nexaworks.rafiq.labTest.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.nexaworks.rafiq.labTest.api.dto.TestResponse;
import com.nexaworks.rafiq.labTest.api.dto.TestResultsResponse;
import com.nexaworks.rafiq.labTest.entity.LabTest;

@Mapper(componentModel = "spring", uses = ResultMapper.class)
public interface TestMapper {

    @Mapping(source = "id", target = "testId")
    @Mapping(target = "fileId", expression = "java(test.getFileId() != null ? test.getFileId().toString() : null)")
    TestResponse toResponse(LabTest test);

    @Mapping(source = "id", target = "testId")
    @Mapping(source = "labResults", target = "tests")
    @Mapping(target = "fileId", expression = "java(test.getFileId() != null ? test.getFileId().toString() : null)")
    @Mapping(target = "date", expression = "java(test.getDate() != null ? java.util.Date.from(test.getDate()) : null)")
    TestResultsResponse mapToTestResponse(LabTest test);
}
