package com.nexaworks.rafiq.service.ServiceImpl;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.nexaworks.rafiq.exception.custom.EmptyFileException;
import com.nexaworks.rafiq.service.*;
import com.nexaworks.rafiq.service.ai.AiService;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExtractorServiceImpl implements PdfExtractorService {

    private final LabTestService labTestService;
    private final AiService aiService;
    private final AuthService authService;

    @Override
    public String extractPdf(MultipartFile pdfFile)
            throws IOException, DocumentException, ExecutionException, InterruptedException {
        if (pdfFile.isEmpty()) {
            throw new EmptyFileException(
                    "The provided PDF file is empty. Please upload a valid file.");
        }
        CompletableFuture<UUID> testId = labTestService.saveTestPdf(pdfFile,
                authService.getAuthenticateUser());
        byte[] pdfBytes = pdfFile.getBytes();
        if (pdfFile.getContentType() != null && pdfFile.getContentType().startsWith("image/")) {
            pdfBytes = convertImage(pdfBytes);
        }
        String result = aiService.extractLabResultsFromPdf(pdfBytes);
        UUID id = testId.get();
        ObjectNode jsonNode = (ObjectNode) new ObjectMapper().readTree(result);
        jsonNode.put("testId", id.toString());
        return jsonNode.toString();
    }

    private static byte[] convertImage(byte[] pdfBytes) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        Image img = Image.getInstance(pdfBytes);
        img.scaleToFit(document.getPageSize().getWidth(), document.getPageSize().getHeight());
        img.setAlignment(Image.ALIGN_CENTER);
        document.add(img);
        document.close();
        pdfBytes = outputStream.toByteArray();
        return pdfBytes;
    }
}
