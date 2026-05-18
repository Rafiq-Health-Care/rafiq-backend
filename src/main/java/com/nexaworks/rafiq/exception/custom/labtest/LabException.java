package com.nexaworks.rafiq.exception.custom.labtest;

public class LabException extends RuntimeException {
    public static final String CODE = "L01";

    public LabException(String message) {
        super(message);
    }
}
