package com.nexaworks.rafiq.service;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;

public interface PdfExtractorService {
    String extractPdf(MultipartFile pdfFile)
            throws IOException, DocumentException, ExecutionException, InterruptedException;
}
