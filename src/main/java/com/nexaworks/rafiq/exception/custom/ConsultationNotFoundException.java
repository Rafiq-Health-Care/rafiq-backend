package com.nexaworks.rafiq.exception.custom;

public class ConsultationNotFoundException extends RuntimeException {
    public static final String CODE = "C01";
    public ConsultationNotFoundException(String consultationNotFound) {
    }
}
