package com.nexaworks.rafiq.dto.notificaiton;

import java.util.Map;

import com.nexaworks.rafiq.entities.enums.ActionStatus;

public record PushNotification(ActionStatus action, String ft,
        Map<String, Object> content) implements Notification {
}
