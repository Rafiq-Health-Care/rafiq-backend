package com.nexaworks.rafiq.service;

import org.springframework.web.multipart.MultipartFile;

public interface AiService {
    String extractLabResultsFromPdf(MultipartFile pdfFile);
}
