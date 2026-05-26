package com.nexaworks.rafiq.rabbit.manager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.entities.enums.ActionStatus;
import com.nexaworks.rafiq.rabbit.event.RefundRequestEvent;
import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;
import com.nexaworks.rafiq.rabbit.publisher.PushNotificationPublisher;
import com.nexaworks.rafiq.rabbit.publisher.RefundEventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefundEventManager {
    private final RefundEventPublisher refundEventPublisher;
    private final PushNotificationPublisher pushNotificationPublisher;

    public void publishRefundRequestEvent(UUID refundId) {
        log.info("Publishing refund request event for refundId: {}", refundId);
        RefundRequestEvent event = new RefundRequestEvent(refundId, Instant.now());
        refundEventPublisher.publish(event);
    }

    public void publishRefundSucceededNotification(UUID id, String fcm, BigDecimal amount) {
        log.info("Publishing refund succeeded notification for refundId: {}", id);
        PushNotification notification = PushNotification.of(ActionStatus.REFUND_SUCCESS, fcm,
                "Your refund has been processed",
                Map.of("amount", amount.toString(), "refundId", id.toString()));
        pushNotificationPublisher.publish(notification);
    }

    public void publishRefundFailedNotification(UUID id, String notificationToken) {
        log.info("Publishing refund failed notification for refundId: {}", id);
        PushNotification notification = PushNotification.of(ActionStatus.REFUND_FAILED,
                "Refund failed please contact support", notificationToken,
                Map.of("refundId", id.toString()));
        pushNotificationPublisher.publish(notification);
    }
}
