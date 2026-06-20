package com.nexaworks.rafiq.exception.custom.user;

public class RegistrationException extends RuntimeException {
    public static final String CODE = "U02";

    public RegistrationException(String message) {
        super(message);
    }
}
