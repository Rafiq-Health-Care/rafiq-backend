package com.nexaworks.rafiq.exception.custom;

public class RefundNotFoundException extends RuntimeException {
    private static final String CODE = "R02";
    public RefundNotFoundException(String message) {
        super(message);
    }
}
