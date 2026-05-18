package com.nexaworks.rafiq.exception.custom.file;

public class FileUploadException extends RuntimeException {
    public static final String CODE = "F03";

    public FileUploadException(String message) {
        super(message);
    }
}
