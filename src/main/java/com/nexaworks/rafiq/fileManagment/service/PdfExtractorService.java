package com.nexaworks.rafiq.fileManagment.service;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.DocumentException;

public interface PdfExtractorService {
    String extractPdf(MultipartFile pdfFile, UUID patientId)
            throws IOException, DocumentException, ExecutionException, InterruptedException;
}
