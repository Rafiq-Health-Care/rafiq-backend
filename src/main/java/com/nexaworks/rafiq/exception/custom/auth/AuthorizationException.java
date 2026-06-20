package com.nexaworks.rafiq.exception.custom.auth;

public class AuthorizationException extends RuntimeException {
    public static final String CODE = "A01";
    public AuthorizationException(String message) {
        super(message);
    }
}
