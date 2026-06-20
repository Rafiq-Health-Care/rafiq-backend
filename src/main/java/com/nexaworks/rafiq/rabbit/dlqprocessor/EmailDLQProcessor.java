package com.nexaworks.rafiq.rabbit.dlqprocessor;

import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.*;
import static com.nexaworks.rafiq.rabbit.consumer.ConsumerUtils.*;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.entities.enums.Level;
import com.nexaworks.rafiq.rabbit.enums.DLQAction;
import com.nexaworks.rafiq.rabbit.notificaiton.EmailNotification;
import com.nexaworks.rafiq.service.alert.AlertService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailDLQProcessor {
    private final AlertService alertService;

    public void processMessage(String failureReason, EmailNotification notification,
            Channel channel, Map<String, Object> headers) throws IOException {
        DLQAction action = classify(failureReason);

        switch (action) {

            case DISCARD -> {
                log.info("[EMAIL-DLQ] Discarding stale message");
            }

            case ALERT -> {
                alertService.sendAlert("EMAIL DLQ CRITICAL",
                        "Reason: " + failureReason + " | User: " + notification.email(),
                        Level.ERROR);
                log.error("[EMAIL-DLQ] Alert sent for critical failure: {}", failureReason);
            }

            case REDRIVE -> {
                alertService.sendAlert("Email DLQ - Needs Redrive", failureReason, Level.WARNING);
                int currentDeathCount = getDeathCount(headers);
                if (currentDeathCount >= 3) {
                    log.error("[PUSH-DLQ] Permanently failed, inspect manually: {}", notification);
                    return;
                }

                handleFailed(channel, headers, failureReason, currentDeathCount + 1,
                        EMAIL_NOTIFICATION_QUEUE, EMAIL_RETRY_ROUTING_KEY,
                        NOTIFICATION_RETRY_EXCHANGE, notification.toString().getBytes());
            }
        }
    }

}
