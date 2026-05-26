package com.nexaworks.rafiq.rabbit.handler;

import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.*;
import static com.nexaworks.rafiq.rabbit.consumer.ConsumerUtils.getDeathCount;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.rabbit.notificaiton.EmailNotification;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("email")
@Slf4j
@RequiredArgsConstructor
public class EmailRetryHandler implements IRetryHandler {
    @Override
    public void handle(String failureReason, EmailNotification notification, Channel channel,
            Map<String, Object> headers) throws IOException {
        int currentDeathCount = getDeathCount(headers);
        int newDeathCount = currentDeathCount + 1;

        log.warn("[EMAIL-DLQ] Transient failure, redriving to retry queue (attempt {}): {}",
                newDeathCount, failureReason);

        Map<String, Object> newHeaders = new java.util.HashMap<>(headers);
        newHeaders.put("x-retry-count", newDeathCount);
        newHeaders.put("x-last-failure-reason", failureReason);
        newHeaders.put("x-last-redriven-at", Instant.now().toString());
        newHeaders.put("x-original-queue", EMAIL_NOTIFICATION_QUEUE);

        channel.basicPublish(NOTIFICATION_RETRY_EXCHANGE, EMAIL_RETRY_ROUTING_KEY,
                new AMQP.BasicProperties.Builder().headers(newHeaders).deliveryMode(2).build(),
                notification.toString().getBytes());

    }
}
