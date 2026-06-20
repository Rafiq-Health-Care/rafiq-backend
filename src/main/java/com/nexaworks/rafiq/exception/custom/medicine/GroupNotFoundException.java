package com.nexaworks.rafiq.exception.custom.medicine;

public class GroupNotFoundException extends RuntimeException {
    public static final String CODE = "M02";

    public GroupNotFoundException(String message) {
        super(message);
    }
}
