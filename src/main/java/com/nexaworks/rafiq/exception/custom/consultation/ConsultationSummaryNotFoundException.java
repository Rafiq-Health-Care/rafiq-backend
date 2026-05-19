package com.nexaworks.rafiq.exception.custom.consultation;

public class ConsultationSummaryNotFoundException extends RuntimeException {
    public static final String CODE = "C09";
    public ConsultationSummaryNotFoundException(String message) {
        super(message);
    }
}
