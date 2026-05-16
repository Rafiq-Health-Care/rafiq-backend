package com.nexaworks.rafiq.rabbit.config;

import static com.nexaworks.rafiq.rabbit.constant.RabbitMQConstant.*;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MessageConverter;

@Configuration
@ConditionalOnBean({AmqpTemplate.class, MessageConverter.class})
public class EmailNotificationConfiguration {

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
    public Binding emailDLQBinding(@Qualifier("notificationDLQExchange") DirectExchange exchange,
            @Qualifier("emailDLQ") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_EMAIL);
    }
    @Bean
    public Binding smsDLQBinding(@Qualifier("notificationDLQExchange") DirectExchange exchange,
            @Qualifier("smsDLQ") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_SMS);
    }
    @Bean
    public Binding pushDLQBinding(@Qualifier("notificationDLQExchange") DirectExchange exchange,
            @Qualifier("pushDLQ") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_PUSH);
    }

    @Bean(name = "emailQueue")
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_EMAIL).build();
    }

    @Bean(name = "smsQueue")
    public Queue smsQueue() {
        return QueueBuilder.durable(SMS_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_SMS).build();
    }

    @Bean(name = "pushQueue")
    public Queue pushQueue() {
        return QueueBuilder.durable(PUSH_NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", NOTIFICATION_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_PUSH).build();
    }
    @Bean(name = "otpQueue")
    public Queue otp() {
        return new Queue(OTP_QUEUE, true);
    }

    @Bean(name = "notificationExchange")
    public TopicExchange exchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }
    @Bean(name = "emailBinding")
    public Binding emailBinding(@Qualifier("notificationExchange") TopicExchange exchange,
            @Qualifier("emailQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_EMAIL);
    }
    @Bean(name = "smsBinding")
    public Binding smsBinding(@Qualifier("notificationExchange") TopicExchange exchange,
            @Qualifier("smsQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_SMS);
    }
    @Bean(name = "pushBinding")
    public Binding pushBinding(@Qualifier("notificationExchange") TopicExchange exchange,
            @Qualifier("pushQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_PUSH);
    }
    @Bean(name = "otpBinding")
    public Binding otpBinding(@Qualifier("notificationExchange") TopicExchange exchange,
            @Qualifier("otpQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(OTP_ROUTING_KEY);
    }
}
