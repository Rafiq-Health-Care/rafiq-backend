package com.nexaworks.rafiq.dto.notificaiton;

import com.nexaworks.rafiq.entities.enums.ActionStatus;

import java.util.Map;

public record PushNotification(ActionStatus action, String notification, Map<String ,Object> content) implements Notification {
}
