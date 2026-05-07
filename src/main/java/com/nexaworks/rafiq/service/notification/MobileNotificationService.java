package com.nexaworks.rafiq.service.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.dto.notificaiton.PushNotification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Qualifier("mobile")
@Slf4j
@RequiredArgsConstructor
public class MobileNotificationService implements NotificationService<PushNotification> {
    @Override
    public void sendNotification(PushNotification notificationDetails) {
        log.info("Sending SMS notification: {}", notificationDetails);
    }
}
