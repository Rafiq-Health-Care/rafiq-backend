package com.nexaworks.rafiq.rabbit.publisher;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpTemplate;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public abstract class BasePublisher<T> implements EventPublisher<T> {
    protected final AmqpTemplate rabbitTemplate;

    protected BasePublisher(AmqpTemplate amqpTemplate) {
        this.rabbitTemplate = amqpTemplate;
    }
    protected void send(String exchange, String routingKey, T event) {
        log.info("Publishing {} to '{}'", event.getClass().getSimpleName(), routingKey);
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published {} to '{}'", event.getClass().getSimpleName(), routingKey);
        } catch (AmqpException e) {
            log.error("Failed to publish {}: {}", event.getClass().getSimpleName(), e.getMessage());
            throw e;
        }
    }
}
