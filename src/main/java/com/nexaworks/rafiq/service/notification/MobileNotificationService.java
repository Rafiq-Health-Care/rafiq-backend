package com.nexaworks.rafiq.service.notification;

import com.nexaworks.rafiq.dto.notificaiton.Notification;
import com.nexaworks.rafiq.dto.notificaiton.PushNotification;
import com.nexaworks.rafiq.dto.notificaiton.SmsNotification;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

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
