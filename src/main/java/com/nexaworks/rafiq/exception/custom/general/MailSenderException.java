package com.nexaworks.rafiq.exception.custom.general;

public class MailSenderException extends RuntimeException {
    public static final String CODE = "G01";

    public MailSenderException(String message) {
        super(message);
    }
}
