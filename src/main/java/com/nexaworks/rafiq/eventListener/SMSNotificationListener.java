package com.nexaworks.rafiq.eventListener;

import com.nexaworks.rafiq.config.RabbitMQConfig;
import com.nexaworks.rafiq.dto.notificaiton.SmsNotification;
import com.nexaworks.rafiq.service.notification.NotificationService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SMSNotificationListener {
    private final NotificationService<SmsNotification> notificationService;

    public SMSNotificationListener(@Qualifier("sms") NotificationService<SmsNotification> notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(queues = RabbitMQConfig.SMS_NOTIFICATION_QUEUE)
    public void handleSMSNotification(SmsNotification smsNotification,
                                      Channel channel,
                                      @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received SMS notification: {}", smsNotification);
        try {
            notificationService.sendNotification(smsNotification);
            channel.basicAck(tag,false);
        }catch (Exception e){
            log.error("Failed to send SMS notification",e);
            channel.basicNack(tag,false,false);
        }
    }

}
