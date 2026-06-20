package com.nexaworks.rafiq.exception.custom.consultation;

public class ConsultationSummaryIsAlreadyCreated extends RuntimeException {
    public static final String CODE = "C10";
    public ConsultationSummaryIsAlreadyCreated(String message) {
        super(message);
    }
}
