package com.nexaworks.rafiq.service.chatbot;

import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.beans.factory.annotation.Value;
import com.nexaworks.rafiq.dto.request.chatbot.ChatTextRequest;
import com.nexaworks.rafiq.dto.response.chatbot.ChatTextResponse;
import com.nexaworks.rafiq.exception.custom.chatbot.ChatbotException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MedicalChatbotServiceImp implements IMedicalChatbotService {

    private final RestClient restClient;

    public MedicalChatbotServiceImp(@Value("${chatbot.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public String sendTextMessage(String userMessage) {
        log.info("Sending text message to chatbot...");

        try {
            ChatTextRequest requestPayload = new ChatTextRequest(userMessage);

            ChatTextResponse response = restClient.post()
                    .uri("/chat/text")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(ChatTextResponse.class);

            log.info("Successfully received text response from chatbot");
            return response != null ? response.reply() : "No response from chatbot.";

        } catch (ResourceAccessException e) {
            log.error("Network failure: Cannot reach Python container. {}", e.getMessage());
            throw new ChatbotException("The AI assistant is currently offline. Please try again later.");

        } catch (HttpServerErrorException e) {
            log.error("AI Server crashed processing text: {}", e.getResponseBodyAsString());
            throw new ChatbotException("The AI assistant encountered an internal error.");

        } catch (HttpClientErrorException e) {
            log.error("Bad request sent to AI: {}", e.getResponseBodyAsString());
            throw new ChatbotException("Failed to process your message due to a data error.");

        }
    }

    @Override
    public String sendVoiceMessage(MultipartFile audioFile) {
        log.info("Sending voice message to chatbot. File size: {} bytes", audioFile.getSize());

        try {
            MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
            bodyBuilder.part("file", audioFile.getResource());

            String response = restClient.post()
                    .uri("/chat/voice")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(bodyBuilder.build())
                    .retrieve()
                    .body(String.class);

            log.info("Successfully received voice response from chatbot");
            return response;

        } catch (ResourceAccessException e) {
            log.error("Network failure: Cannot reach Python container. {}", e.getMessage());
            throw new ChatbotException("The AI assistant is currently offline. Please try again later.");

        } catch (HttpServerErrorException e) {
            log.error("AI Server crashed processing text: {}", e.getResponseBodyAsString());
            throw new ChatbotException("The AI assistant encountered an internal error.");

        } catch (HttpClientErrorException e) {
            log.error("Bad request sent to AI: {}", e.getResponseBodyAsString());
            throw new ChatbotException("Failed to process your message due to a data error.");

        }
    }

}
