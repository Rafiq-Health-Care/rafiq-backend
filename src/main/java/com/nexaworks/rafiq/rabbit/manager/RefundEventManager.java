package com.nexaworks.rafiq.rabbit.manager;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.rabbit.event.RefundRequestEvent;
import com.nexaworks.rafiq.rabbit.publisher.RefundEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefundEventManager {
    private final RefundEventPublisher refundEventPublisher;

    public void publishRefundRequestEvent(UUID refundId) {
        log.info("Publishing refund request event for refundId: {}", refundId);
        RefundRequestEvent event = new RefundRequestEvent(refundId, Instant.now());
        refundEventPublisher.publish(event);
    }
}
