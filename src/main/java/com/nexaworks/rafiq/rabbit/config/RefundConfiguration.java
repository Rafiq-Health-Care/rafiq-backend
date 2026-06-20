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
public class RefundConfiguration {
    @Bean("refundQueue")
    public Queue refundQueue() {
        return QueueBuilder.durable(REFUND_REQUEST_QUEUE)
                .withArgument("x-dead-letter-exchange", REFUND_REQUEST_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", REFUND_REQUEST_DLQ_ROUTING_KEY).build();
    }
    @Bean("refundDLQ")
    public Queue refundDLQ() {
        return QueueBuilder.durable(REFUND_REQUEST_DLQ_QUEUE).build();
    }
    @Bean("refundExchange")
    public DirectExchange refundExchange() {
        return new DirectExchange(REFUND_REQUEST_EXCHANGE);
    }
    @Bean("refundDLQExchange")
    public DirectExchange refundDLQExchange() {
        return new DirectExchange(REFUND_REQUEST_DLQ_EXCHANGE);
    }
    @Bean("refundBinding")
    public Binding refundBinding(@Qualifier("refundExchange") DirectExchange exchange,
            @Qualifier("refundQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(REFUND_REQUEST_ROUTING_KEY);
    }
    @Bean("refundDLQBinding")
    public Binding refundDLQBinding(@Qualifier("refundDLQExchange") DirectExchange exchange,
            @Qualifier("refundDLQ") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(REFUND_REQUEST_DLQ_ROUTING_KEY);
    }
}
