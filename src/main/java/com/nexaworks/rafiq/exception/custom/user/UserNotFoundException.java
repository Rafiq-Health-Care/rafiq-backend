package com.nexaworks.rafiq.exception.custom.user;

public class UserNotFoundException extends RuntimeException {
    public static final String CODE = "U06";

    public UserNotFoundException(String message) {
        super(message);
    }
}
