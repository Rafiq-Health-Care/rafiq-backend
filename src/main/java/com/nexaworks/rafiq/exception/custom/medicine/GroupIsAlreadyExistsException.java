package com.nexaworks.rafiq.exception.custom.medicine;

public class GroupIsAlreadyExistsException extends RuntimeException {
    public static final String CODE = "M01";

    public GroupIsAlreadyExistsException(String message) {
        super(message);
    }
}
