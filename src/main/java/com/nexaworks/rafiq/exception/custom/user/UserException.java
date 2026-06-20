package com.nexaworks.rafiq.exception.custom.user;

public class UserException extends RuntimeException {
    public static final String CODE = "U05";

    public UserException(String message) {
        super(message);
    }
}
