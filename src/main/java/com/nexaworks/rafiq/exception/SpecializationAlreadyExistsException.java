package com.nexaworks.rafiq.exception;

public class SpecializationAlreadyExistsException extends RuntimeException {
    
    public SpecializationAlreadyExistsException(String message) {
        super(message);
    }
    
    public SpecializationAlreadyExistsException(String field, String value) {
        super("Specialization with " + field + " '" + value + "' already exists");
    }
}
