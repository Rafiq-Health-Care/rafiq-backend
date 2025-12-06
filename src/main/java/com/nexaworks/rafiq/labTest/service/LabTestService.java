package com.nexaworks.rafiq.labTest.service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.labTest.api.dto.TestResultRequest;
import com.nexaworks.rafiq.labTest.entity.LabResult;
import com.nexaworks.rafiq.labTest.entity.LabTest;
import com.nexaworks.rafiq.user.entity.model.User;

import jakarta.validation.Valid;

public interface LabTestService {
    void addTest(UUID testId, String testName, Date testDate, List<LabResult> entity,UUID patientId);

    Page<LabTest> getAll(int page, int size, String sort, String direction,UUID patientId);

    LabTest getTest(UUID testId,UUID patientId);

    void deleteTest(UUID testId,UUID patientId);

    Integer deleteAll(UUID patientId);

    void update(UUID testId, @Valid TestResultRequest testResultRequest, List<LabResult> entity,UUID patientId);

    CompletableFuture<UUID> saveTestPdf(MultipartFile file, User user,UUID patientId) throws IOException;
}
