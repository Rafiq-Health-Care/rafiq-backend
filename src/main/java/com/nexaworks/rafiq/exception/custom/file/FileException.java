package com.nexaworks.rafiq.exception.custom.file;

public class FileException extends RuntimeException {
    public static final String CODE = "F02";

    public FileException(String message) {
        super(message);
    }
}
