package com.nexaworks.rafiq.idempotency.storage;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record IdempotentResponse(int statusCode, byte[] body, String contentType, Instant timestamp,
        Map<String, String> headers, Status status) {
    public static IdempotentResponse inFlight() {
        return new IdempotentResponse(0, null, null, Instant.now(), Map.of(), Status.IN_FLIGHT);
    }

    @JsonIgnore
    public boolean isInFlight() {
        return status == Status.IN_FLIGHT;
    }
}
