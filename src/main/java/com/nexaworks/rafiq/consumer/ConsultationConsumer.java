package com.nexaworks.rafiq.consumer;

import com.nexaworks.rafiq.dto.notificaiton.PushNotification;
import com.nexaworks.rafiq.entities.enums.ActionStatus;
import com.nexaworks.rafiq.service.consultation.ConsultationPreparationService;
import com.nexaworks.rafiq.service.consultation.ConsultationService;
import com.nexaworks.rafiq.service.notification.NotificationService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.UUID;

import static com.nexaworks.rafiq.constant.RabbitMQConstant.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConsultationConsumer {

    private static final int MAX_RETRIES = 3;

    private final ConsultationService consultationService;
    private final ConsultationPreparationService consultationPreparationService;
    private final AmqpTemplate rabbitTemplate;

    @RabbitListener(queues = CONSULTATION_EXPIRATION_QUEUE)
    public void handleExpiration(String consultationId, Message message, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received expiration request for consultation: {}", consultationId);
        try {
            consultationService.expire(consultationId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to expire consultation: {}", consultationId, e);
            retryOrDlq(consultationId, message, channel, tag,
                    CONSULTATION_EXPIRATION_EXCHANGE,
                    CONSULTATION_EXPIRATION_RETRY_ROUTING_KEY,
                    CONSULTATION_EXPIRATION_DLQ_ROUTING_KEY);
        }
    }

    @RabbitListener(queues = CONSULTATION_PREPARATION_QUEUE)
    public void handlePreparation(String consultationId, Message message, Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        log.info("Received preparation request for consultation: {}", consultationId);
        try {
            consultationPreparationService.prepare(UUID.fromString(consultationId));
            channel.basicAck(tag, false);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID '{}', routing directly to DLQ", consultationId);
            rabbitTemplate.convertAndSend(CONSULTATION_PREPARATION_EXCHANGE, CONSULTATION_PREPARATION_DLQ_ROUTING_KEY, consultationId);
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("Failed to prepare consultation: {}", consultationId, e);
            retryOrDlq(consultationId, message, channel, tag,
                    CONSULTATION_PREPARATION_EXCHANGE,
                    CONSULTATION_PREPARATION_RETRY_ROUTING_KEY,
                    CONSULTATION_PREPARATION_DLQ_ROUTING_KEY);
        }
    }

    private void retryOrDlq(String id, Message message, Channel channel, long tag,
                            String exchange, String retryKey, String dlqKey) throws IOException {
        int retryCount = getRetryCount(message);
        if (retryCount >= MAX_RETRIES) {
            log.error("Max retries reached for {}, sending to DLQ", id);
            rabbitTemplate.convertAndSend(exchange, dlqKey, id);
        } else {
            int next = retryCount + 1;
            log.warn("Retrying {}/{} for consultation: {}", next, MAX_RETRIES, id);
            rabbitTemplate.convertAndSend(exchange, retryKey, id,
                    msg -> { msg.getMessageProperties().setHeader("x-retry-count", next); return msg; });
        }
        channel.basicAck(tag, false);
    }

    private int getRetryCount(Message message) {
        Object count = message.getMessageProperties().getHeaders().get("x-retry-count");
        return count == null ? 0 : (int) count;
    }
}