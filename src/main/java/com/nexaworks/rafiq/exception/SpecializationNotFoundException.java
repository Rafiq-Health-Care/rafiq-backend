package com.nexaworks.rafiq.exception;

import java.util.UUID;

public class SpecializationNotFoundException extends RuntimeException {
    
    public SpecializationNotFoundException(String message) {
        super(message);
    }
    
    public SpecializationNotFoundException(UUID specializationId) {
        super("Specialization with ID " + specializationId + " not found");
    }
    
    public SpecializationNotFoundException(String field, String value) {
        super("Specialization with " + field + " '" + value + "' not found");
    }
}
