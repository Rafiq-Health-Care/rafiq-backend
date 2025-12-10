package com.nexaworks.rafiq.ai.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nexaworks.rafiq.shared.dto.TestRequest;

public interface AnalysisService {
    String analysis(String analyzeLabResults, List<TestRequest> tests)
            throws JsonProcessingException;
}
