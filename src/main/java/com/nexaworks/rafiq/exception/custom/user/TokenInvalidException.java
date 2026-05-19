package com.nexaworks.rafiq.exception.custom.user;

public class TokenInvalidException extends RuntimeException {
    public static final String CODE = "U03";

    public TokenInvalidException(String message) {
        super(message);
    }
}
