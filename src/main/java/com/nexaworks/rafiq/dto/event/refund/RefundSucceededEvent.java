package com.nexaworks.rafiq.dto.event.refund;

import java.time.Instant;
import java.util.UUID;

public record RefundSucceededEvent(UUID refundId, Instant occurredAt) {
}
