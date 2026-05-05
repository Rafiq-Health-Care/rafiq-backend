package com.nexaworks.rafiq.service.notification;

import com.nexaworks.rafiq.dto.notificaiton.Notification;

import java.util.Map;

public interface NotificationService<T extends Notification> {
    void sendNotification(T notification);
}
