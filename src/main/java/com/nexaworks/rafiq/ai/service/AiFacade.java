package com.nexaworks.rafiq.ai.service;

import java.io.IOException;

import com.itextpdf.text.DocumentException;

public interface AiFacade {
    String extractLabResultsFromPdf(byte[] pdfBytes) throws DocumentException, IOException;
}
