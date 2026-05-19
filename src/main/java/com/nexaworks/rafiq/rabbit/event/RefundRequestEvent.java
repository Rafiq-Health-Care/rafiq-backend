package com.nexaworks.rafiq.rabbit.event;

import java.time.Instant;
import java.util.UUID;

public record RefundRequestEvent(UUID refundId, Instant occurredAt) {
}
