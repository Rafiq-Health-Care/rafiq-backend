package com.nexaworks.rafiq.exception.custom.auth;

public class GoogleAuthException extends RuntimeException {
    public static final String CODE = "A02";
    public GoogleAuthException(String message) {
        super(message);
    }
}
