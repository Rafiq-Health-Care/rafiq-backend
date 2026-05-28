package com.nexaworks.rafiq.idempotency.exceptions;

public class RequestInFlightException extends RuntimeException {
    public RequestInFlightException(String message) {
        super(message);
    }
}
