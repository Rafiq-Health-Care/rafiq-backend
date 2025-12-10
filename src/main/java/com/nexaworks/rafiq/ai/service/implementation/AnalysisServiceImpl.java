package com.nexaworks.rafiq.ai.service.implementation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.ai.service.AiService;
import com.nexaworks.rafiq.ai.service.AnalysisService;
import com.nexaworks.rafiq.shared.dto.TestRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisServiceImpl implements AnalysisService {
    private final AiService aiService;
    private final ObjectMapper objectMapper;
    @Override
    public String analysis(String analyzeLabResults, List<TestRequest> tests)
            throws JsonProcessingException {
        String aiResponse = aiService.analysisData(analyzeLabResults, tests);
        log.info("AI Analysis Response: {}", aiResponse);
        String cleanedAiResponse = aiResponse.replace("```json", "").replace("```", "").trim();
        JsonNode jsonNode = objectMapper.readTree(cleanedAiResponse);
        String report = jsonNode.get("report").asText();
        // todo throw event to update the test report in the database
        log.info("Report: {}", report);
        return report;
    }
}
