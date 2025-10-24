package com.nexaworks.rafiq.service.ServiceImpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nexaworks.rafiq.exception.custom.EmptyFileException;
import com.nexaworks.rafiq.service.AiService;
import com.nexaworks.rafiq.service.LabTestService;
import com.nexaworks.rafiq.service.PdfExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExtractorServiceImpl implements PdfExtractorService {

    private final ChatClient chatClient;
    private final LabTestService labTestService;
    private final AiService aiService;

    @Override
    public String extractPdf(MultipartFile pdfFile) throws IOException {
        if (pdfFile.isEmpty()) {
            throw new EmptyFileException("The provided PDF file is empty. Please upload a valid file.");
        }
        CompletableFuture<UUID> testId = labTestService.saveTestPdf(pdfFile);
        String result = aiService.extractLabResultsFromPdf(pdfFile);



    }



}
