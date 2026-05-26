package com.nexaworks.rafiq.service.notification;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.nexaworks.rafiq.rabbit.notificaiton.Notification;

public interface NotificationService<T extends Notification> {
    void sendNotification(T notification) throws FirebaseMessagingException;
}
