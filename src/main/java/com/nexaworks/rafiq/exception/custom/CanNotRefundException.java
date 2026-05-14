package com.nexaworks.rafiq.exception.custom;

public class CanNotRefundException extends RuntimeException {
    public static final String CODE = "R01";
    public CanNotRefundException(String message) {
        super(message);
    }
}
