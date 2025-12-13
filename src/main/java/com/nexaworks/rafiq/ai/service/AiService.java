package com.nexaworks.rafiq.ai.service;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.itextpdf.text.DocumentException;

public interface AiService {
    String extractLabResultsFromPdf(byte[] pdfFile) throws IOException, DocumentException;

    String analysisData(String analyzeLabResults, List<?> tests) throws JsonProcessingException;
}
