package com.nexaworks.rafiq.rabbit.dlqprocessor;

import static com.nexaworks.rafiq.rabbit.consumer.ConsumerUtils.classify;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexaworks.rafiq.entities.enums.Level;
import com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant;
import com.nexaworks.rafiq.rabbit.enums.DLQAction;
import com.nexaworks.rafiq.rabbit.handler.IRetryHandler;
import com.nexaworks.rafiq.rabbit.notificaiton.EmailNotification;
import com.nexaworks.rafiq.service.alert.AlertService;
import com.nexaworks.rafiq.service.messagelog.IMessageLogService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailDLQProcessor {
    private final AlertService alertService;
    private final IRetryHandler retryHandler;
    private final IMessageLogService messageLogService;
    private final ObjectMapper objectMapper;

    public void processMessage(String failureReason, EmailNotification notification,
            Channel channel, Map<String, Object> headers) throws IOException {
        DLQAction action = classify(failureReason);
        messageLogService.persist(failureReason, objectMapper.writeValueAsString(notification),
                headers, action, notification.notificationId(),
                RabbitMQConstant.EMAIL_NOTIFICATION_QUEUE);

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
                retryHandler.handle(failureReason, notification, channel, headers);

                alertService.sendAlert("EMAIL DLQ - Needs Redrive", failureReason, Level.WARNING);
            }
        }
    }

}
