package com.nexaworks.rafiq.rabbit.consumer;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.Headers;

import com.nexaworks.rafiq.rabbit.enums.DLQAction;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

public class ConsumerUtils {
    public static int getDeathCount(Map<String, Object> headers) {
        Object count = headers.get("x-death-count");
        if (count instanceof Number n)
            return n.intValue();

        Object xDeath = headers.get("x-death");
        if (xDeath instanceof java.util.List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> deathMap) {
                Object c = deathMap.get("count");
                return c instanceof Number n ? n.intValue() : 0;
            }
        }
        return 0;
    }
    public static String getFailureReason(Map<String, Object> headers) {
        Object custom = headers.get("x-failure-reason");
        if (custom != null)
            return custom.toString();

        Object xDeath = headers.get("x-death");
        if (xDeath instanceof java.util.List<?> list && !list.isEmpty()) {
            Object first = list.get(0);
            if (first instanceof Map<?, ?> deathMap) {
                Object reason = deathMap.get("reason");
                return reason != null ? reason.toString() : "unknown";
            }
        }
        return "unknown";
    }

    public static DLQAction classify(String reason) {
        if (reason == null)
            return DLQAction.DISCARD;

        String r = reason.toLowerCase();

        if (r.contains("timeout") || r.contains("connection refused") || r.contains("smtp"))
            return DLQAction.REDRIVE;

        if (r.contains("invalid ") || r.contains("unsubscribed") || r.contains("bounce"))
            return DLQAction.FIX_AND_DISCARD;

        if (r.contains("expired") || r.contains("already sent"))
            return DLQAction.DISCARD;

        if (r.contains("auth") || r.contains("quota exceeded") || r.contains("billing"))
            return DLQAction.ALERT;

        return DLQAction.DISCARD;
    }
    static void handleFailed(Channel channel, @Headers Map<String, Object> headers, Exception e,
            int newDeathCount, String notificationQueue, String routingKeyPush, String exchange,
            byte[] messageBytes) throws IOException {
        Map<String, Object> newHeaders = new HashMap<>(headers);
        newHeaders.put("x-retry-count", newDeathCount);
        newHeaders.put("x-last-failure-reason", e.getMessage());
        newHeaders.put("x-last-redriven-at", Instant.now().toString());
        newHeaders.put("x-original-queue", notificationQueue);

        channel.basicPublish(exchange, routingKeyPush,
                new AMQP.BasicProperties.Builder().headers(newHeaders).deliveryMode(2).build(),
                messageBytes);
    }
}
