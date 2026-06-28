package com.nexaworks.rafiq.service.chatbot;

import org.springframework.web.multipart.MultipartFile;

public interface IMedicalChatbotService {

    public String sendTextMessage(String userMessage);
    public String sendVoiceMessage(MultipartFile audioFile);

}
