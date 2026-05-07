package com.nexaworks.rafiq.consumer;

import static com.nexaworks.rafiq.constant.RabbitMQConstant.SMS_NOTIFICATION_QUEUE;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.notificaiton.PushNotification;
import com.nexaworks.rafiq.service.notification.NotificationService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PushNotificationConsumer {
    private final NotificationService<PushNotification> notificationService;

    public PushNotificationConsumer(
            @Qualifier("mobile") NotificationService<PushNotification> notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = SMS_NOTIFICATION_QUEUE)
    public void handleSMSNotification(PushNotification pushNotification, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received SMS notification: {}", pushNotification);
        try {
            notificationService.sendNotification(pushNotification);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to send SMS notification", e);
            channel.basicNack(tag, false, false);
        }
    }
}
