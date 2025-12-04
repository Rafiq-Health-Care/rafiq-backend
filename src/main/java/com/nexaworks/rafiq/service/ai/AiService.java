package com.nexaworks.rafiq.service.ai;

import java.io.IOException;

import com.itextpdf.text.DocumentException;

public interface AiService {
    String extractLabResultsFromPdf(byte[] pdfFile) throws IOException, DocumentException;
}
