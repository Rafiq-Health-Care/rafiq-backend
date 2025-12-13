package com.nexaworks.rafiq.doctor.exception;

public class SpecializationValidationException extends RuntimeException {

    public SpecializationValidationException(String message) {
        super(message);
    }

    public SpecializationValidationException(String field, String reason) {
        super("Specialization validation failed for field '" + field + "': " + reason);
    }
}
