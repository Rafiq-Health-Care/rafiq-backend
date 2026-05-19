package com.nexaworks.rafiq.rabbit.publisher;

import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.NOTIFICATION_EXCHANGE;
import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.ROUTING_KEY_PUSH;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j

public class PushNotificationPublisher extends BasePublisher<PushNotification> {

    public PushNotificationPublisher(AmqpTemplate amqpTemplate) {
        super(amqpTemplate);
    }

    @Override
    public void publish(PushNotification notification) {
        send(NOTIFICATION_EXCHANGE, ROUTING_KEY_PUSH, notification);
    }
}
