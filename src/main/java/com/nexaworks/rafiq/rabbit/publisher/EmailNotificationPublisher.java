package com.nexaworks.rafiq.rabbit.publisher;

import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.*;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.rabbit.notificaiton.EmailNotification;

import lombok.extern.slf4j.Slf4j;

@Component
@Qualifier("email")
@Slf4j
public class EmailNotificationPublisher extends BasePublisher<EmailNotification> {
    public EmailNotificationPublisher(AmqpTemplate amqpTemplate) {
        super(amqpTemplate);
    }

    @Override
    public void publish(EmailNotification event) {
        send(NOTIFICATION_EXCHANGE, ROUTING_KEY_EMAIL, event);
    }
}
