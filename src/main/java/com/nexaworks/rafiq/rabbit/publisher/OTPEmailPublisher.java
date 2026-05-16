package com.nexaworks.rafiq.rabbit.publisher;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant;
import com.nexaworks.rafiq.rabbit.notificaiton.EmailNotification;

import lombok.extern.slf4j.Slf4j;

@Component
@Qualifier("otpEmail")
@Slf4j
public class OTPEmailPublisher extends BasePublisher<EmailNotification> {
    public OTPEmailPublisher(AmqpTemplate amqpTemplate) {
        super(amqpTemplate);
    }

    @Override
    public void publish(EmailNotification event) {
        send(RabbitMQConstant.NOTIFICATION_EXCHANGE, RabbitMQConstant.OTP_ROUTING_KEY, event);
    }
}
