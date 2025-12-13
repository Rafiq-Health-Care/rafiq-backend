package com.nexaworks.rafiq.ai.listener;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.ai.service.AnalysisService;
import com.nexaworks.rafiq.ai.utils.Prompt;
import com.nexaworks.rafiq.shared.event.labTest.LabTestCreatedEvent;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class AnalysisListener {
    private final AnalysisService analysisService;
    private final ObjectMapper objectMapper;

    @Async
    @EventListener(LabTestCreatedEvent.class)
    public void handleLabTestCreatedEvent(LabTestCreatedEvent event)
            throws JsonProcessingException {
        log.info("Analysis Listener - Lab Test Created Event Received for Test ID: {}",
                event.testId());
        analysisService.analysis(Prompt.ANALYZE_LAB_RESULTS, event.tests());
    }
}
