package com.nexaworks.rafiq.exception.custom.consultation;

public class ConsultationInvalidException extends RuntimeException {
    public static final String CODE = "C11";
    public ConsultationInvalidException(String message) {
        super(message);
    }
}
