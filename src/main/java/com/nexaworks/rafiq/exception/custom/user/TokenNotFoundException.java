package com.nexaworks.rafiq.exception.custom.user;

public class TokenNotFoundException extends RuntimeException {
    public static final String CODE = "U04";

    public TokenNotFoundException(String message) {
        super(message);
    }
}
