package com.nexaworks.rafiq.service.labReports;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.request.labTest.TestResultRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.labTest.TestResponse;
import com.nexaworks.rafiq.entities.User;

import jakarta.validation.Valid;

public interface LabTestService {
    void addTest(@Valid TestResultRequest request);

    PageResponse<TestResponse> getAll(int page, int size, String sort, String direction);

    com.nexaworks.rafiq.dto.response.labTest.TestResultsResponse getTest(UUID testId);

    void deleteTest(UUID testId);

    Integer deleteAll();

    void update(UUID testId, @Valid TestResultRequest testResultRequest);

    CompletableFuture<UUID> saveTestPdf(MultipartFile file, User user) throws IOException;
}
