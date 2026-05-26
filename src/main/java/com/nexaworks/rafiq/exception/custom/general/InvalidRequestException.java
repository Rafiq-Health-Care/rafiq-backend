package com.nexaworks.rafiq.exception.custom.general;

public class InvalidRequestException extends RuntimeException {
    public static final String CODE = "G02";
    public InvalidRequestException(String message) {
        super(message);
    }
}
