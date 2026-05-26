package com.nexaworks.rafiq.service.notification;

import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;

public interface INotificationPersistenceService {
    void saveNotification(PushNotification notification, User user);
}
