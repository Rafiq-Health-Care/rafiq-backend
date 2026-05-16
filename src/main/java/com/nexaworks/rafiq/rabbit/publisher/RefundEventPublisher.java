package com.nexaworks.rafiq.rabbit.publisher;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.constant.RabbitMQConstant;
import com.nexaworks.rafiq.dto.event.refund.RefundRequestEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j

public class RefundEventPublisher extends BasePublisher<RefundRequestEvent> {

    public RefundEventPublisher(AmqpTemplate amqpTemplate) {
        super(amqpTemplate);
    }

    @Override
    public void publish(RefundRequestEvent event) {
        send(RabbitMQConstant.REFUND_REQUEST_EXCHANGE, RabbitMQConstant.REFUND_REQUEST_ROUTING_KEY,
                event);
    }
}
