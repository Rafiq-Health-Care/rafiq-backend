package com.nexaworks.rafiq.exception.custom.labtest;

public class LabTestException extends RuntimeException {
    public static final String CODE = "L02";

    public LabTestException(String message) {
        super(message);
    }
}
