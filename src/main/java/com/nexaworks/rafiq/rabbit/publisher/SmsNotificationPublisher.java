package com.nexaworks.rafiq.rabbit.publisher;

import static com.nexaworks.rafiq.constant.RabbitMQConstant.*;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.notificaiton.SmsNotification;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SmsNotificationPublisher extends BasePublisher<SmsNotification> {
    protected SmsNotificationPublisher(AmqpTemplate amqpTemplate) {
        super(amqpTemplate);
    }

    @Override
    public void publish(SmsNotification event) {
        send(NOTIFICATION_EXCHANGE, ROUTING_KEY_SMS, event);
    }
}
