package com.nexaworks.rafiq.exception.custom.file;

public class EmptyFileException extends RuntimeException {
    public static final String CODE = "F01";

    public EmptyFileException(String message) {
        super(message);
    }
}
