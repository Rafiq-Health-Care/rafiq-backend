package com.nexaworks.rafiq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    public static final String EMAIL_NOTIFICATION_QUEUE = "notification.email";
    public static final String SMS_NOTIFICATION_QUEUE = "notification.sms";
    public static final String PUSH_NOTIFICATION_QUEUE = "notification.push";

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    public static final String ROUTING_KEY_EMAIL = "notification.email";
    public static final String ROUTING_KEY_SMS = "notification.sms";
    public static final String ROUTING_KEY_PUSH = "notification.push";

    public static final String EMAIL_DLQ = "notification.email.dlq";
    public static final String SMS_DLQ = "notification.sms.dlq";
    public static final String PUSH_DLQ = "notification.push.dlq";

    public static final String NOTIFICATION_DLQ_EXCHANGE = "notification.dlq.exchange";

    public static final String CONSULTATION_EXPIRATION_QUEUE = "consultation.expiration";
    public static final String CONSULTATION_EXPIRATION_EXCHANGE = "consultation.expiration.exchange";
    public static final String CONSULTATION_EXPIRATION_ROUTING_KEY = "consultation.expiration";

    public static final String CONSULTATION_EXPIRATION_DLQ = "consultation.expiration.dlq";
    public static final String CONSULTATION_EXPIRATION_DLQ_ROUTING_KEY = "consultation.expiration.dlq";

    public static final String CONSULTATION_EXPIRATION_RETRY_QUEUE = "consultation.expiration.retry";
    public static final String CONSULTATION_EXPIRATION_RETRY_ROUTING_KEY = "consultation.expiration.retry";

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");

        return new CustomExchange(
                CONSULTATION_EXPIRATION_EXCHANGE,
                "x-delayed-message",
                true,
                false,
                args
        );
    }
    @Bean
    public Queue consultationExpirationQueue() {
        return QueueBuilder.durable(CONSULTATION_EXPIRATION_QUEUE)
                .withArgument("x-dead-letter-exchange", CONSULTATION_EXPIRATION_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CONSULTATION_EXPIRATION_RETRY_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue consultationExpirationRetryQueue() {
        return QueueBuilder.durable(CONSULTATION_EXPIRATION_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange",CONSULTATION_EXPIRATION_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CONSULTATION_EXPIRATION_ROUTING_KEY)
                .withArgument("x-message-ttl", 60_000L)
                .build();
    }

    @Bean
    public Queue consultationExpirationDlq() {
        return QueueBuilder.durable(CONSULTATION_EXPIRATION_DLQ)
                .build();
    }


    @Bean
    public Binding consultationExpirationBinding() {
        return BindingBuilder
                .bind(consultationExpirationQueue())
                .to(delayedExchange())
                .with(CONSULTATION_EXPIRATION_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding consultationExpirationRetryBinding() {
        return BindingBuilder
                .bind(consultationExpirationRetryQueue())
                .to(delayedExchange())
                .with(CONSULTATION_EXPIRATION_RETRY_ROUTING_KEY)
                .noargs();
    }

    @Bean
    public Binding consultationExpirationDlqBinding() {
        return BindingBuilder
                .bind(consultationExpirationDlq())
                .to(delayedExchange())
                .with(CONSULTATION_EXPIRATION_DLQ_ROUTING_KEY)
                .noargs();
    }

    @Bean(name = "emailDLQ")
    public Queue emailDLQ() {
        return new Queue(EMAIL_DLQ, true);
    }
    @Bean(name = "smsDLQ")
    public Queue smsDLQ() {
        return new Queue(SMS_DLQ, true);
    }
    @Bean(name = "pushDLQ")
    public Queue pushDLQ() {
        return new Queue(PUSH_DLQ, true);
    }
    @Bean(name = "notificationDLQExchange")
    public DirectExchange notificationDLQExchange() {
        return new DirectExchange(NOTIFICATION_DLQ_EXCHANGE);
    }

    @Bean
    public Binding emailDLQBinding(@Qualifier("notificationDLQExchange") DirectExchange exchange,@Qualifier("emailDLQ") Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ROUTING_KEY_EMAIL);
    }
    @Bean
    public Binding smsDLQBinding(@Qualifier("notificationDLQExchange")DirectExchange exchange,@Qualifier("smsDLQ") Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ROUTING_KEY_SMS);
    }
    @Bean
    public Binding pushDLQBinding(@Qualifier("notificationDLQExchange")DirectExchange exchange,@Qualifier("pushDLQ")Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ROUTING_KEY_PUSH);
    }

    @Bean(name = "emailQueue")
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_EMAIL)
                .build();
    }

    @Bean(name = "smsQueue")
    public Queue smsQueue() {
        return QueueBuilder.durable(SMS_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_SMS)
                .build();
    }

    @Bean(name = "pushQueue")
    public Queue pushQueue() {
        return QueueBuilder.durable(PUSH_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_PUSH)
                .build();
    }

    @Bean(name = "notificationExchange")
    public TopicExchange exchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }
    @Bean(name = "emailBinding")
    public Binding emailBinding(@Qualifier("notificationExchange") TopicExchange exchange,@Qualifier("emailQueue") Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ROUTING_KEY_EMAIL);
    }
    @Bean(name = "smsBinding")
    public Binding smsBinding(@Qualifier("notificationExchange") TopicExchange exchange,@Qualifier("smsQueue") Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ROUTING_KEY_SMS);
    }
    @Bean(name = "pushBinding")
    public Binding pushBinding(@Qualifier("notificationExchange") TopicExchange exchange,@Qualifier("pushQueue") Queue queue) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(ROUTING_KEY_PUSH);
    }
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory,MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }




}
