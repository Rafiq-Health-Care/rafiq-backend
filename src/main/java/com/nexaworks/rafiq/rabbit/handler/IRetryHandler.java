package com.nexaworks.rafiq.rabbit.handler;

import java.io.IOException;
import java.util.Map;

import com.nexaworks.rafiq.rabbit.notificaiton.EmailNotification;
import com.rabbitmq.client.Channel;

public interface IRetryHandler {
    void handle(String failureReason, EmailNotification notification, Channel channel,
            Map<String, Object> headers) throws IOException;
}
