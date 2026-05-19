package com.nexaworks.rafiq.rabbit.consumer;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant;
import com.nexaworks.rafiq.rabbit.event.RefundRequestEvent;
import com.nexaworks.rafiq.service.refund.IRefundProcessingService;
import com.rabbitmq.client.Channel;
import com.stripe.exception.StripeException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class RefundConsumer {
    private final IRefundProcessingService refundProcessingService;

    @RabbitListener(queues = RabbitMQConstant.REFUND_REQUEST_QUEUE)
    public void handleRefundRequest(RefundRequestEvent event, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received refund request event: {}", event);
        try {
            refundProcessingService.beginProcessing(event.refundId());
            channel.basicAck(tag, false);
        } catch (StripeException | IOException e) {
            log.error("Failed to process refund request", e);
            channel.basicNack(tag, false, false);
        }

    }
    @RabbitListener(queues = RabbitMQConstant.REFUND_REQUEST_DLQ_QUEUE)
    public void handleRefundRequestDLQ(RefundRequestEvent event, Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        log.info("Refund request event permanently failed, inspect manually: {}", event);
    }

}
