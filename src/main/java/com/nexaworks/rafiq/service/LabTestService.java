package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.request.TestResultRequest;
import com.nexaworks.rafiq.entities.LabResult;
import jakarta.validation.Valid;

import java.util.List;

public interface LabTestService {
    void addTest(@Valid TestResultRequest testResultRequest, List<LabResult> entity);
}
