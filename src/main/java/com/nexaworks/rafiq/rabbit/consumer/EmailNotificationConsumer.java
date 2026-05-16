package com.nexaworks.rafiq.rabbit.consumer;

import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.EMAIL_NOTIFICATION_QUEUE;
import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.OTP_QUEUE;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.rabbit.notificaiton.EmailNotification;
import com.nexaworks.rafiq.service.notification.NotificationService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EmailNotificationConsumer {
    private final NotificationService<EmailNotification> notificationService;

    public EmailNotificationConsumer(
            @Qualifier("email") NotificationService<EmailNotification> notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = {EMAIL_NOTIFICATION_QUEUE,OTP_QUEUE})
    public void handleEmailNotification(EmailNotification emailNotification, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received email notification: {}", emailNotification);
        try {
            notificationService.sendNotification(emailNotification);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to send email notification", e);
            channel.basicNack(tag, false, false);
        }
    }
}
