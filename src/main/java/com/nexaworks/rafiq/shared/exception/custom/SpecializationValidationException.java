package com.nexaworks.rafiq.shared.exception.custom;

public class SpecializationValidationException extends RuntimeException {

    public SpecializationValidationException(String message) {
        super(message);
    }

    public SpecializationValidationException(String field, String reason) {
        super("Specialization validation failed for field '" + field + "': " + reason);
    }
}
