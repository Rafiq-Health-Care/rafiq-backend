package com.nexaworks.rafiq.ai.service;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.itextpdf.text.DocumentException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AiFacadeImpl implements AiFacade {
    private final AiService aiService;
    @Override
    public String extractLabResultsFromPdf(byte[] pdfBytes) throws DocumentException, IOException {
        return aiService.extractLabResultsFromPdf(pdfBytes);
    }
}
