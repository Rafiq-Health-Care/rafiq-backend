package com.nexaworks.rafiq.ai.service.implementation;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import com.nexaworks.rafiq.ai.service.AiService;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.DocumentException;
import com.nexaworks.rafiq.ai.client.Gemini;
import com.nexaworks.rafiq.ai.client.dto.ContentPart;
import com.nexaworks.rafiq.ai.client.dto.InlineDataPart;
import com.nexaworks.rafiq.ai.client.dto.Part;
import com.nexaworks.rafiq.ai.client.dto.RequestBodyDTO;
import com.nexaworks.rafiq.ai.utils.Prompt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService implements AiService {
    private final Gemini gemini;
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    @Override
    public String extractLabResultsFromPdf(byte[] pdfBytes) throws IOException, DocumentException {

        String encodedPdf = Base64.getEncoder().encodeToString(pdfBytes);
        RequestBodyDTO requestBody = prepareGeminiRequest(encodedPdf);

        return handleGeminiResponse(requestBody);
    }

    @Override
    public String analysisData(String prompt, List<?> data) throws JsonProcessingException {
        String preparedPrompt = prompt + objectMapper.writeValueAsString(data);
        return chatClient.prompt()
                .user(preparedPrompt)
                .call()
                .content();
    }

    @NotNull
    private String handleGeminiResponse(RequestBodyDTO requestBody) throws JsonProcessingException {
        String result = gemini.getResult(requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(result);
        String jsonResponse = jsonNode.get("candidates").get(0).get("content").get("parts").get(0)
                .get("text").asText();
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
}
