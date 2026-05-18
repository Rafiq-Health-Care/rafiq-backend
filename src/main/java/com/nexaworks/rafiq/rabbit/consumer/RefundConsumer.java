package com.nexaworks.rafiq.rabbit.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant;
import com.nexaworks.rafiq.rabbit.event.RefundRequestEvent;
import com.nexaworks.rafiq.service.refund.RefundService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefundConsumer {
    private final RefundService refundService;

    @RabbitListener(queues = RabbitMQConstant.REFUND_REQUEST_QUEUE)
    public void handleRefundRequest(RefundRequestEvent event, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        // todo handle idempotency
        log.info("Refund request received:{}", event.paymentId());

    }

}
