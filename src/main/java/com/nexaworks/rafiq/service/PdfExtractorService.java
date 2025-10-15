package com.nexaworks.rafiq.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

public interface PdfExtractorService {
    String extractPdf(MultipartFile pdfFile) throws IOException;
}
