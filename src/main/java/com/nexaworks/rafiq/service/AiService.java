package com.nexaworks.rafiq.service;

import com.itextpdf.text.DocumentException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AiService {
    String extractLabResultsFromPdf(MultipartFile pdfFile) throws IOException, DocumentException;
}
