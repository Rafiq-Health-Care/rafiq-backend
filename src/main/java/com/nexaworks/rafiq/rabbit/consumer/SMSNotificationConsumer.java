package com.nexaworks.rafiq.rabbit.consumer;

import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.SMS_DLQ;
import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.SMS_NOTIFICATION_QUEUE;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.rabbit.notificaiton.SmsNotification;
import com.nexaworks.rafiq.service.notification.NotificationService;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SMSNotificationConsumer {
    private final NotificationService<SmsNotification> notificationService;

    public SMSNotificationConsumer(
            @Qualifier("sms") NotificationService<SmsNotification> notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = SMS_NOTIFICATION_QUEUE)
    public void handleSMSNotification(SmsNotification smsNotification, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received SMS notification: {}", smsNotification);
        try {
            notificationService.sendNotification(smsNotification);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to send SMS notification", e);
            channel.basicNack(tag, false, false);
        }
    }
    @RabbitListener(queues = SMS_DLQ)
    public void handleSmsDLQ(SmsNotification notification, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.error("Sms notification permanently failed, inspect manually: {}", notification);
        // todo handle failed sms notification
        channel.basicAck(tag, false);
    }

}
