package com.nexaworks.rafiq.service.ServiceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nexaworks.rafiq.service.ImageService;
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
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Service
@RequiredArgsConstructor
@Slf4j
public class PdfExtractorServiceImpl implements PdfExtractorService {

    private final ChatClient chatClient;
    private final ImageService imageService;
    private final LabTestService labTestService;

    @Override
    public String extractPdf(MultipartFile pdfFile) throws IOException {
        CompletableFuture<UUID> testId = labTestService.saveTestPdf(pdfFile);
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
                            "\n" +
                            "\" "+ text +
                            "\n" +
                            "**Strictly adhere to these rules:**\n" +
                            "\n" +
                            "1. Identify all distinct **medical lab test names**, their associated **numerical results**, the **units** of measure, and their **status**.\n" +
                            "2. Ignore reference intervals, methodologies, interpretations, doctor names, patient demographics, and report metadata.\n" +
                            "3. For tests that explicitly mention an abnormal status (e.g., 'H' for High, 'L' for Low, 'A' for Abnormal, or 'Non Reactive'):\n" +
                            "   - Use the provided status directly.\n" +
                            "   - If the result is non-numerical (e.g., 'Non Reactive', 'Positive', 'Negative'), set `\"result\"` to that text and `\"unit\"` to an empty string.\n" +
                            "4. For tests **without an explicit status**, infer it automatically using **standard adult reference ranges**:\n" +
                            "   - `\"H\"` → High (above normal range)\n" +
                            "   - `\"L\"` → Low (below normal range)\n" +
                            "   - `\"N\"` → Normal (within range)\n" +
                            "   - `\"Unknown\"` → if the range cannot be inferred\n" +
                            "5. For calculated ratios (e.g., CHOL/HDL Ratio), include the calculated value and infer `\"status\"` if possible.\n" +
                            "6. Return the output as **one valid JSON object** following the exact structure below — no text or explanations, just the JSON.\n" +
                            "\n" +
                            "**Required JSON Format (Exact Structure):**\n" +
                            "{\n" +
                            "  \"tests\": [\n" +
                            "    {\"testName\": \"Hemoglobin\", \"result\": \"13.5\", \"unit\": \"g/dL\", \"status\": \"N\"},\n" +
                            "    {\"testName\": \"Ferritin\", \"result\": \"20\", \"unit\": \"µg/L\", \"status\": \"L\"},\n" +
                            "    {\"testName\": \"HIV\", \"result\": \"Non Reactive\", \"unit\": \"\", \"status\": \"Non Reactive\"}\n" +
                            "  ]\n" +
                            "}\n")
                    .call()
                    .chatResponse()).getResult()
                   .getOutput().getText();

            if (json!=null) {
                json = json.replace("```", "");
                json = json.replace("json", "");
            }
            UUID test = testId.get();
            ObjectNode jsonNode = (ObjectNode) new ObjectMapper().readTree(json);
            jsonNode.put("testId", test.toString());


            return jsonNode.toString();
        } catch (IOException | TesseractException | InterruptedException | ExecutionException e) {
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
