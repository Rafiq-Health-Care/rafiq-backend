package com.nexaworks.rafiq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.nexaworks.rafiq.dto.request.chatbot.ChatTextRequest;
import com.nexaworks.rafiq.service.chatbot.MedicalChatbotServiceImp;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatbotController {

    private final MedicalChatbotServiceImp chatbotService;

    @PostMapping("/text")
    public ResponseEntity<String> chatWithText(@RequestBody ChatTextRequest request) {
        String response = chatbotService.sendTextMessage(request.message());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/voice", consumes = {"multipart/form-data"})
    public ResponseEntity<String> chatWithVoice(@RequestParam("audio") MultipartFile audioFile) {
        String response = chatbotService.sendVoiceMessage(audioFile);
        return ResponseEntity.ok(response);
    }

}
