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
public class PayoutConfiguration {

    // Main Payout Queue with DLQ routing
    @Bean("payoutQueue")
    public Queue payoutQueue() {
        return QueueBuilder.durable(PAYOUT_REQUEST_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYOUT_REQUEST_DLQ_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PAYOUT_REQUEST_DLQ_ROUTING_KEY).build();
    }

    // DLQ for failed payouts
    @Bean("payoutDLQ")
    public Queue payoutDLQ() {
        return QueueBuilder.durable(PAYOUT_REQUEST_DLQ_QUEUE).build();
    }

    // Retry Queue with 1-minute TTL, redrives back to main queue
    @Bean("payoutRetryQueue")
    public Queue payoutRetryQueue() {
        return QueueBuilder.durable(PAYOUT_RETRY_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYOUT_REQUEST_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", PAYOUT_REQUEST_ROUTING_KEY).ttl(60000) // 1
                                                                                                  // minute
                                                                                                  // delay
                                                                                                  // before
                                                                                                  // retry
                .build();
    }

    // Main Exchange for payout requests
    @Bean("payoutExchange")
    public DirectExchange payoutExchange() {
        return new DirectExchange(PAYOUT_REQUEST_EXCHANGE);
    }

    // DLQ Exchange for failed payouts
    @Bean("payoutDLQExchange")
    public DirectExchange payoutDLQExchange() {
        return new DirectExchange(PAYOUT_REQUEST_DLQ_EXCHANGE);
    }

    // Retry Exchange
    @Bean("payoutRetryExchange")
    public DirectExchange payoutRetryExchange() {
        return new DirectExchange(PAYOUT_RETRY_EXCHANGE);
    }

    // Binding: Main Queue to Main Exchange
    @Bean("payoutBinding")
    public Binding payoutBinding(@Qualifier("payoutExchange") DirectExchange exchange,
            @Qualifier("payoutQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(PAYOUT_REQUEST_ROUTING_KEY);
    }

    // Binding: DLQ to DLQ Exchange
    @Bean("payoutDLQBinding")
    public Binding payoutDLQBinding(@Qualifier("payoutDLQExchange") DirectExchange exchange,
            @Qualifier("payoutDLQ") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(PAYOUT_REQUEST_DLQ_ROUTING_KEY);
    }

    // Binding: Retry Queue to Retry Exchange
    @Bean("payoutRetryBinding")
    public Binding payoutRetryBinding(@Qualifier("payoutRetryExchange") DirectExchange exchange,
            @Qualifier("payoutRetryQueue") Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with(PAYOUT_RETRY_ROUTING_KEY);
    }
}
