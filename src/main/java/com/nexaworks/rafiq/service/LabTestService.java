package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.request.TestResultRequest;
import com.nexaworks.rafiq.entities.LabResult;
import com.nexaworks.rafiq.entities.LabTest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LabTestService {
    void addTest(@Valid TestResultRequest testResultRequest, List<LabResult> entity);

    Page<LabTest> getAll(int page, int size, String sort, String direction);

    LabTest getTest(UUID testId);

    void deleteTest(UUID testId);

    Integer deleteAll();

    void update(UUID testId, @Valid TestResultRequest testResultRequest, List<LabResult> entity);
    public CompletableFuture<UUID> saveTestPdf(MultipartFile file) throws IOException;
}
