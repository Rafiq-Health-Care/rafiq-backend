package com.nexaworks.rafiq.exception.custom.consultation;

public class ConsultationSlotTakenException extends RuntimeException {
    public static final String CODE = "C08";

    public ConsultationSlotTakenException(String message) {
        super(message);
    }
}
