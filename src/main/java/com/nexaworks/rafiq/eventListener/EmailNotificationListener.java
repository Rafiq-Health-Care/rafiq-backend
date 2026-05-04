package com.nexaworks.rafiq.eventListener;

import com.nexaworks.rafiq.config.RabbitMQConfig;
import com.nexaworks.rafiq.dto.notificaiton.EmailNotification;
import com.nexaworks.rafiq.service.notification.NotificationService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@Slf4j
public class EmailNotificationListener {
    private final NotificationService<EmailNotification> notificationService;

    public EmailNotificationListener(@Qualifier("email") NotificationService<EmailNotification> notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.EMAIL_NOTIFICATION_QUEUE)
    public void handleEmailNotification(EmailNotification emailNotification, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received email notification: {}", emailNotification);
        try {
            notificationService.sendNotification(emailNotification);
            channel.basicAck(tag,false);
        }catch (Exception e){
            log.error("Failed to send email notification",e);
            channel.basicNack(tag,false,false);
        }
    }
}
