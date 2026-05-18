package com.nexaworks.rafiq.exception.custom.medicine;

public class ReminderNotFound extends RuntimeException {
    public static final String CODE = "M06";

    public ReminderNotFound(String message) {
        super(message);
    }
}
