package com.nexaworks.rafiq.rabbit.dlqprocessor;

import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.*;
import static com.nexaworks.rafiq.rabbit.consumer.ConsumerUtils.*;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.entities.enums.Level;
import com.nexaworks.rafiq.rabbit.enums.DLQAction;
import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;
import com.nexaworks.rafiq.service.alert.AlertService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class PushDLQProcessor {
    private final AlertService alertService;

    public void processMessage(String failureReason, PushNotification notification, Channel channel,
            Map<String, Object> headers) throws IOException {
        DLQAction action = classify(failureReason);

        switch (action) {

            case DISCARD -> {
                log.info("[PUSH-DLQ] Discarding stale message");
            }

            case ALERT -> {
                alertService.sendAlert("PUSH DLQ CRITICAL",
                        "Reason: " + failureReason + " | User: " + notification.notificationId(),
                        Level.ERROR);
                log.error("[PUSh-DLQ] Alert sent for critical failure: {}", failureReason);
            }

            case REDRIVE -> {
                alertService.sendAlert("PUSH DLQ - Needs Redrive", failureReason, Level.WARNING);
                int currentDeathCount = getDeathCount(headers);
                if (currentDeathCount >= 3) {
                    log.error("[PUSH-DLQ] Permanently failed, inspect manually: {}", notification);
                    return;
                }
                handleFailed(channel, headers, failureReason, currentDeathCount + 1,
                        PUSH_NOTIFICATION_QUEUE, PUSH_RETRY_ROUTING_KEY,
                        NOTIFICATION_RETRY_EXCHANGE, notification.toString().getBytes());
            }
        }
    }
}
