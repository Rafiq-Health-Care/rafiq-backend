package com.nexaworks.rafiq.exception.custom;

public class AuthorizationException extends RuntimeException {
    public static final String CODE = "A01";
    public AuthorizationException(String message) {
        super(message);
    }
}
