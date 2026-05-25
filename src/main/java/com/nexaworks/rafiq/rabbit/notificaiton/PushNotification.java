package com.nexaworks.rafiq.rabbit.notificaiton;

import java.util.Map;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.ActionStatus;

public record PushNotification(UUID notificationId, ActionStatus action, String ft, String body,
        Map<String, String> data) implements Notification {

    public PushNotification {
        notificationId = notificationId != null ? notificationId : UUID.randomUUID();
    }

    public static PushNotification of(ActionStatus action, String ft, String body,
            Map<String, String> data) {
        return new PushNotification(UUID.randomUUID(), action, ft, body, data);
    }
}