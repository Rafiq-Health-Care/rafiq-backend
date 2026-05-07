package com.nexaworks.rafiq.consumer;

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

import static com.nexaworks.rafiq.constant.RabbitMQConstant.EMAIL_NOTIFICATION_QUEUE;

@Component
@Slf4j
public class EmailNotificationConsumer {
    private final NotificationService<EmailNotification> notificationService;

    public EmailNotificationConsumer(@Qualifier("email") NotificationService<EmailNotification> notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = EMAIL_NOTIFICATION_QUEUE)
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
