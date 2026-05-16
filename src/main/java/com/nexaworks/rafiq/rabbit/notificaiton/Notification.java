package com.nexaworks.rafiq.rabbit.notificaiton;

public sealed interface Notification permits EmailNotification, SmsNotification, PushNotification {
}
