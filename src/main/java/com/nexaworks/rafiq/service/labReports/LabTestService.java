package com.nexaworks.rafiq.service.labReports;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.request.labTest.TestResultRequest;
import com.nexaworks.rafiq.entities.LabResult;
import com.nexaworks.rafiq.entities.LabTest;
import com.nexaworks.rafiq.entities.User;

import jakarta.validation.Valid;

public interface LabTestService {
    void addTest(UUID testId, String testName, Date testDate, List<LabResult> entity);

    Page<LabTest> getAll(int page, int size, String sort, String direction);

    LabTest getTest(UUID testId);

    void deleteTest(UUID testId);

    Integer deleteAll();

    void update(UUID testId, @Valid TestResultRequest testResultRequest, List<LabResult> entity);

    CompletableFuture<UUID> saveTestPdf(MultipartFile file, User user) throws IOException;
}
