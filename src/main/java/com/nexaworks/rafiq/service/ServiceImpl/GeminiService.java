package com.nexaworks.rafiq.service.ServiceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;
import com.nexaworks.rafiq.client.Gemini;

import com.nexaworks.rafiq.dto.client.*;
import com.nexaworks.rafiq.service.AiService;
import com.nexaworks.rafiq.utils.Prompt;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService implements AiService {
    private final Gemini gemini;

    @Override
    public String extractLabResultsFromPdf(MultipartFile pdfFile) throws IOException, DocumentException {
        byte[] pdfBytes = pdfFile.getBytes();
      if (Objects.equals(pdfFile.getContentType(), "image/")) {
          pdfBytes = convertImage(pdfBytes);
      }
        String encodedPdf = Base64.getEncoder().encodeToString(pdfBytes);
        RequestBodyDTO requestBody = prepareGeminiRequest(encodedPdf);


        String result = gemini.getResult(requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
       String jsonResponse = jsonNode.get("candidates").get(0).get("content").get("parts").get(0).get("text").asText();
       jsonResponse = jsonResponse.replace("```json", "").replace("```", "").trim();
       return jsonResponse;

    }

    @NotNull
    private static RequestBodyDTO prepareGeminiRequest(String encodedPdf) {
        InlineDataPart inlineData = new InlineDataPart("application/pdf", encodedPdf);
        Part pdfPart = new Part(inlineData, null);
        Part textPart = new Part(null, Prompt.EXTRACT_PDF);

        ContentPart content = new ContentPart(List.of(pdfPart, textPart));
        return new RequestBodyDTO(List.of(content));
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
