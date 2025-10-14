package com.nexaworks.rafiq.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface PdfExtractorService {
    String extractPdf(MultipartFile pdfFile);
}
