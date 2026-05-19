package com.nexaworks.rafiq.exception.custom.payment;

public class PaymentException extends RuntimeException {
    private static final String CODE = "P01";
    public PaymentException(String message) {
        super(message);
    }
}
