package com.nexaworks.rafiq.service;

import com.itextpdf.text.DocumentException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface PdfExtractorService {
    String extractPdf(MultipartFile pdfFile) throws IOException, DocumentException, ExecutionException, InterruptedException;
}
