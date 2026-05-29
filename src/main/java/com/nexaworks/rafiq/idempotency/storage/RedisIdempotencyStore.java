package com.nexaworks.rafiq.idempotency.storage;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RedisIdempotencyStore implements IdempotencyStore {
    private final RedisTemplate<String, String> template;
    private final long ttlSeconds;
    private final ObjectMapper objectMapper;

    public RedisIdempotencyStore(RedisTemplate<String, String> template, ObjectMapper objectMapper,
            long ttlSeconds) {
        this.template = template;
        this.ttlSeconds = ttlSeconds;
        this.objectMapper = objectMapper;
    }

    @Override
    public void set(String key, IdempotentResponse idempotentResponse) {
        try {
            String json = objectMapper.writeValueAsString(idempotentResponse);
            template.opsForValue().set(key, json, ttlSeconds, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize idempotent response:", e);
        }

    }

    @Override
    public IdempotentResponse get(String key) {
        String json = template.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            IdempotentResponse response = objectMapper.readValue(json, IdempotentResponse.class);
            if (!response.isInFlight()
                    && response.timestamp().plusSeconds(ttlSeconds).isBefore(Instant.now())) {
                template.delete(key);
                return null;
            }
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize idempotent response", e);
        }
    }

    @Override
    public void delete(String key) {
        template.delete(key);
    }

    @Override
    public void setInFlight(String key) {
        try {
            String json = objectMapper.writeValueAsString(IdempotentResponse.inFlight());
            template.opsForValue().set(key, json, 30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set in-flight state", e);
        }

    }
}
