package com.nexaworks.rafiq.exception.custom.consultation;

public class CanNotCancelConsultation extends RuntimeException {
    public static final String CODE = "C07";
    public CanNotCancelConsultation(String message) {
        super(message);
    }
}
