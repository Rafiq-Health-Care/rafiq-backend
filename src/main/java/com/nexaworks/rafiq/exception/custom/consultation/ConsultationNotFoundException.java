package com.nexaworks.rafiq.exception.custom.consultation;

public class ConsultationNotFoundException extends RuntimeException {
    public static final String CODE = "C06";
    public ConsultationNotFoundException(String consultationNotFound) {
        super(consultationNotFound);
    }
}
