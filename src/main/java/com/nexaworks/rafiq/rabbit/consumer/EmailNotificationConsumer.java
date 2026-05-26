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

import com.nexaworks.rafiq.rabbit.dlqprocessor.EmailDLQProcessor;
import com.nexaworks.rafiq.rabbit.notificaiton.EmailNotification;
import com.nexaworks.rafiq.service.notification.NotificationService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailNotificationConsumer {
    private final NotificationService<EmailNotification> notificationService;
    private final EmailDLQProcessor emailDLQProcessor;

    public EmailNotificationConsumer(
            @Qualifier("email") NotificationService<EmailNotification> notificationService,
            EmailDLQProcessor emailDLQProcessor) {
        this.notificationService = notificationService;
        this.emailDLQProcessor = emailDLQProcessor;
    }

    @RabbitListener(queues = EMAIL_NOTIFICATION_QUEUE)
    public void handleEmailNotification(EmailNotification notification, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag, @Headers Map<String, Object> headers)
            throws IOException {
        try {
            notificationService.sendNotification(notification);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            int currentDeathCount = getDeathCount(headers);
            int newDeathCount = currentDeathCount + 1;

            log.warn("[EMAIL-DLQ] Transient failure, redriving to retry queue (attempt {}): {}",
                    newDeathCount, e.getMessage());

            handleFailed(channel, headers, e.getMessage(), newDeathCount, EMAIL_NOTIFICATION_QUEUE,
                    ROUTING_KEY_EMAIL, NOTIFICATION_DLQ_EXCHANGE,
                    notification.toString().getBytes());

        }
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = OTP_QUEUE)
    public void handleOtpNotification(EmailNotification notification, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received OTP notification: {}", notification);
        try {
            notificationService.sendNotification(notification);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to send OTP notification {}", e.getMessage());
            channel.basicNack(tag, false, false);
        }

    }
    @RabbitListener(queues = {EMAIL_DLQ})
    public void handleEmailDLQ(EmailNotification emailNotification, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag, @Headers Map<String, Object> headers)
            throws IOException {
        log.error("Email notification permanently failed, inspect manually: {}", emailNotification);

        String failureReason = ConsumerUtils.getFailureReason(headers);
        emailDLQProcessor.processMessage(failureReason, emailNotification, channel, headers);

        channel.basicAck(tag, false);
    }

}
