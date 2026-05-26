package com.nexaworks.rafiq.rabbit.consumer;

import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.*;
import static com.nexaworks.rafiq.rabbit.consumer.ConsumerUtils.getDeathCount;
import static com.nexaworks.rafiq.rabbit.consumer.ConsumerUtils.handleFailed;

import java.io.IOException;
import java.util.Map;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.rabbit.dlqprocessor.PushDLQProcessor;
import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;
import com.nexaworks.rafiq.service.notification.NotificationService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PushNotificationConsumer {
    private final NotificationService<PushNotification> notificationService;
    private final PushDLQProcessor pushDLQProcessor;

    public PushNotificationConsumer(
            @Qualifier("push") NotificationService<PushNotification> notificationService,
            PushDLQProcessor pushDLQProcessor) {
        this.notificationService = notificationService;
        this.pushDLQProcessor = pushDLQProcessor;
    }

    @RabbitListener(queues = SMS_NOTIFICATION_QUEUE)
    public void handleSMSNotification(PushNotification pushNotification, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag, @Headers Map<String, Object> headers)
            throws IOException {
        log.info("Received SMS notification: {}", pushNotification);
        try {
            notificationService.sendNotification(pushNotification);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            int currentDeathCount = getDeathCount(headers);
            int newDeathCount = currentDeathCount + 1;
            log.warn("[PUSH-DLQ] Transient failure, redriving to retry queue (attempt {}): {}",
                    newDeathCount, e.getMessage());
            handleFailed(channel, headers, e.getMessage(), newDeathCount, PUSH_NOTIFICATION_QUEUE,
                    ROUTING_KEY_PUSH, NOTIFICATION_DLQ_EXCHANGE,
                    pushNotification.toString().getBytes());
        }
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = PUSH_DLQ)
    public void handlePushDLQ(PushNotification notification, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag, @Headers Map<String, Object> headers)
            throws IOException {
        log.error("Push notification permanently failed, inspect manually: {}", notification);
        String failureReason = ConsumerUtils.getFailureReason(headers);
        pushDLQProcessor.processMessage(failureReason, notification, channel, headers);
        channel.basicAck(tag, false);
    }
}
