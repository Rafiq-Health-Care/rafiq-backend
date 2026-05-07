package com.nexaworks.rafiq.dto.notificaiton;

public sealed interface Notification permits EmailNotification, SmsNotification, PushNotification {
}
