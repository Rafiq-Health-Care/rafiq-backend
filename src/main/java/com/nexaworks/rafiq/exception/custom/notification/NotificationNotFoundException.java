package com.nexaworks.rafiq.exception.custom.notification;

public class NotificationNotFoundException extends RuntimeException {
    public static final String CODE = "N01";
    public NotificationNotFoundException(String message) {
        super(message);
    }
}
