package com.nexaworks.rafiq.labTest.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;

import com.nexaworks.rafiq.labTest.api.dto.TestResultRequest;
import com.nexaworks.rafiq.labTest.entity.LabResult;
import com.nexaworks.rafiq.labTest.entity.LabTest;

import jakarta.validation.Valid;

public interface LabTestService {
    void addTest(UUID testId, String testName, Date testDate, List<LabResult> entity,
            UUID patientId);

    Page<LabTest> getAll(int page, int size, String sort, String direction, UUID patientId);

    LabTest getTest(UUID testId, UUID patientId);

    void deleteTest(UUID testId, UUID patientId);

    Integer deleteAll(UUID patientId);

    void update(UUID testId, @Valid TestResultRequest testResultRequest, List<LabResult> entity,
            UUID patientId);
}
