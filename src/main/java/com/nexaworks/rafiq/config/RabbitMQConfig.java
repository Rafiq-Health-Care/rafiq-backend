package com.nexaworks.rafiq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EMAIL_NOTIFICATION_QUEUE = "notification.email";
    public static final String SMS_NOTIFICATION_QUEUE = "notification.sms";
    public static final String PUSH_NOTIFICATION_QUEUE = "notification.push";

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    public static final String ROUTING_KEY_EMAIL = "notification.email";
    public static final String ROUTING_KEY_SMS = "notification.sms";
    public static final String ROUTING_KEY_PUSH = "notification.push";

    @Bean(name = "emailQueue")
    public Queue emailQueue() {
        return new Queue(EMAIL_NOTIFICATION_QUEUE, true);
    }
    @Bean(name = "smsQueue")
    public Queue smsQueue() {
        return new Queue(SMS_NOTIFICATION_QUEUE, true);
    }
    @Bean(name = "pushQueue")
    public Queue pushQueue() {
        return new Queue(PUSH_NOTIFICATION_QUEUE, true);
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
