package com.nexaworks.rafiq.rabbit.notificaiton;

import java.util.Map;

import com.nexaworks.rafiq.entities.enums.ActionStatus;

public record PushNotification(ActionStatus title, String ft, String body,
        Map<String, Object> data) implements Notification {
}
