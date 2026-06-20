package com.nexaworks.rafiq.idempotency.storage;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryIdempotencyStore implements IdempotencyStore {

    private final Map<String, IdempotentResponse> store = new ConcurrentHashMap<>();
    private final long ttlSeconds;

    public InMemoryIdempotencyStore(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public void set(String key, IdempotentResponse response) {
        store.put(key, response);
    }

    @Override
    public void setInFlight(String key) {
        store.put(key, IdempotentResponse.inFlight());
    }

    @Override
    public IdempotentResponse get(String key) {
        IdempotentResponse response = store.get(key);
        if (response == null)
            return null;

        if (!response.isInFlight()
                && response.timestamp().plusSeconds(ttlSeconds).isBefore(Instant.now())) {
            store.remove(key);
            return null;
        }

        return response;
    }

    @Override
    public void delete(String key) {
        store.remove(key);
    }
}