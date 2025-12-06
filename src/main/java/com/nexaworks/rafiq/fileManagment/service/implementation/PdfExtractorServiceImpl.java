package com.nexaworks.rafiq.fileManagment.service.implementation;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.nexaworks.rafiq.fileManagment.service.FileService;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.nexaworks.rafiq.fileManagment.exception.EmptyFileException;
import com.nexaworks.rafiq.ai.service.AiService;
import com.nexaworks.rafiq.fileManagment.service.PdfExtractorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExtractorServiceImpl implements PdfExtractorService {


    private final AiService aiService;
    private final CloudinaryService cloudinaryService;
    private final FileService fileService;


    @Override
    public String extractPdf(MultipartFile pdfFile,UUID patientId)
            throws IOException, DocumentException, ExecutionException, InterruptedException {
        if (pdfFile.isEmpty()) {
            throw new EmptyFileException(
                    "The provided PDF file is empty. Please upload a valid file.");
        }

        CompletableFuture<UUID> fileId =fileService.saveFileAsync(pdfFile, patientId);
        byte[] pdfBytes = pdfFile.getBytes();
        if (pdfFile.getContentType() != null && pdfFile.getContentType().startsWith("image/")) {
            pdfBytes = convertImage(pdfBytes);
        }
        String result = aiService.extractLabResultsFromPdf(pdfBytes);
        UUID id = fileId.get();
        ObjectNode jsonNode = (ObjectNode) new ObjectMapper().readTree(result);
        jsonNode.put("fileId", id.toString());
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
