package com.nexaworks.rafiq.exception.custom.user;

public class InvalidPasswordException extends RuntimeException {
    public static final String CODE = "U01";

    public InvalidPasswordException(String message) {
        super(message);
    }
}
