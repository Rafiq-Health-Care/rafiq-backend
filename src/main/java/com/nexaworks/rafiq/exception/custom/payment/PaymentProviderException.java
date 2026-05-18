package com.nexaworks.rafiq.exception.custom.payment;

public class PaymentProviderException extends RuntimeException {
    private static final String CODE = "P02";
    public PaymentProviderException(String message) {
        super(message);
    }
}
