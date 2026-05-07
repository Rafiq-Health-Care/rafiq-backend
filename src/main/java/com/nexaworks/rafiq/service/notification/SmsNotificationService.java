package com.nexaworks.rafiq.service.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.dto.notificaiton.SmsNotification;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Qualifier("sms")
public class SmsNotificationService implements NotificationService<SmsNotification> {
    @Override
    public void sendNotification(SmsNotification notification) {
        log.info("Sending SMS notification: {}", notification);
    }
}
