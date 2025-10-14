package com.nexaworks.rafiq.service.ServiceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExtractorServiceImpl implements PdfExtractorService {

    private final ChatClient chatClient;

    @Override
    public String extractPdf(MultipartFile pdfFile) {
        try {
            InputStream inputStream = pdfFile.getInputStream();
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Extracted pdf file: {}", text);
            if (text.trim().isEmpty()) {
                text = ocrExtractor(document);
            }
          String json  = Objects.requireNonNull(chatClient.prompt()
                    .user("You are an expert medical data extractor. Your task is to process the following medical lab report text.\n" +
                            "\n" + text +
                            "**Strictly adhere to these rules:**\n" +
                            "1.  Identify all distinct **medical lab test names**, their associated **numerical results**, and the **units** of measure.\n" +
                            "2.  Ignore reference intervals, methodologies, interpretations, doctor names, patient demographics, and report metadata.\n" +
                            "3.  For tests that report an abnormal status (e.g., 'H' for High, 'L' for Low, or 'Non Reactive') only include the numerical result and unit if present. If the result is a non-numerical status (e.g., \"Non Reactive\"), use the status as the result, and if no unit is available, use an empty string for the unit.\n" +
                            "4.  For calculated ratios (e.g., CHOL/HDL Ratio), use the calculated numerical value as the result.\n" +
                            "5.  Return the output as a single, valid JSON array, strictly conforming to the provided format.\n" +
                            "\n" +
                            "**Required JSON Format (Exact Structure):**\n" +
                            "*{ tests= {" +
                            "    {\"testName\": \"Hemoglobin\", \"result\": \"13.5\", \"unit\": \"g/dL\"},\n" +
                            "    {\"testName\": \"Ferritin\", \"result\": \"20\", \"unit\": \"µg/L\"}\n" +
                            " }}@")
                    .call()
                    .chatResponse()).getResult()
                   .getOutput().getText();

            if (json!=null) {
                json = json.replace("```", "");
                json = json.replace("json", "");
            }

            return json;
        } catch (IOException | TesseractException e) {
            throw new RuntimeException(e);
        }

    }

    private String ocrExtractor(PDDocument document) throws IOException, TesseractException {
        PDFRenderer renderer = new PDFRenderer(document);
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        tesseract.setLanguage("eng");
        StringBuilder text = new StringBuilder();
        for (int page = 0; page < document.getNumberOfPages(); page++) {
            BufferedImage image = renderer.renderImageWithDPI(page,300);
            String result = tesseract.doOCR(image);
            text.append(result).append("\n");
        }
        return text.toString();
    }


}
