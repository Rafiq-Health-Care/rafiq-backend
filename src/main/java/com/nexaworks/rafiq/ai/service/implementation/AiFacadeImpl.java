package com.nexaworks.rafiq.ai.service.implementation;

import java.io.IOException;

import com.nexaworks.rafiq.ai.service.AiFacade;
import com.nexaworks.rafiq.ai.service.AiService;
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
