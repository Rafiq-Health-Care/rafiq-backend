package com.nexaworks.rafiq.exception.custom.chatbot;

public class ChatbotException extends RuntimeException {

    public ChatbotException(String message) {
        super(message);
    }

    public ChatbotException(String message, Throwable cause) {
        super(message, cause);
    }

}
