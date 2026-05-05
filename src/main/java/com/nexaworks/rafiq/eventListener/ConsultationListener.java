package com.nexaworks.rafiq.eventListener;

import com.nexaworks.rafiq.config.RabbitMQConfig;
import com.nexaworks.rafiq.service.consultation.ConsultationService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;



import static com.nexaworks.rafiq.config.RabbitMQConfig.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConsultationListener {
    private final ConsultationService consultationService;
    private final AmqpTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.CONSULTATION_EXPIRATION_QUEUE)
    public void handleExpiration(String consultationId, Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag){
        log.info("Received expiration request for consultation: {}", consultationId);
        try {
            consultationService.expire(consultationId);
            log.info("Consultation expired successfully: {}", consultationId);
            channel.basicAck(tag,false);

        } catch (Exception e) {
            log.error("Failed to expire consultation", e.getCause());
            int retryCount = getRetryCount(message);

            if (retryCount >= 3) {
                rabbitTemplate.convertAndSend(
                        CONSULTATION_EXPIRATION_EXCHANGE,
                        CONSULTATION_EXPIRATION_DLQ_ROUTING_KEY,
                        consultationId
                );
                log.error("Failed to expire consultation after 3 retries");
                return;
            }
            rabbitTemplate.convertAndSend(
                    CONSULTATION_EXPIRATION_EXCHANGE,
                    CONSULTATION_EXPIRATION_RETRY_ROUTING_KEY,
                    consultationId,
                    msg -> {
                        msg.getMessageProperties().setHeader("x-retry-count", retryCount + 1);
                        return msg;
                    }
            );
            log.error("Failed to expire consultation, retrying...");
        }
    }
    private int getRetryCount(Message message) {
        Object count = message.getMessageProperties().getHeaders().get("x-retry-count");
        return count == null ? 0 : (int) count;
    }
}
