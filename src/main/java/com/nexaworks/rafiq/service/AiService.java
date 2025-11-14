package com.nexaworks.rafiq.service;

import com.itextpdf.text.DocumentException;
import java.io.IOException;

public interface AiService {
  String extractLabResultsFromPdf(byte[] pdfFile) throws IOException, DocumentException;
}
