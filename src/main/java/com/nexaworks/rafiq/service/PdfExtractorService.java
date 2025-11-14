package com.nexaworks.rafiq.service;

import com.itextpdf.text.DocumentException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.springframework.web.multipart.MultipartFile;

public interface PdfExtractorService {
    String extractPdf(MultipartFile pdfFile)
            throws IOException, DocumentException, ExecutionException, InterruptedException;
}
