package com.nexaworks.rafiq.exception.custom.consultation;

public class ConsultationOverlappingException extends RuntimeException {
    public static final String CODE = "C05";
    public ConsultationOverlappingException(String s) {
        super(s);
    }
}
