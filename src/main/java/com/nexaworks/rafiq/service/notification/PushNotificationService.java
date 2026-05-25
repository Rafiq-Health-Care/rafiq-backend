package com.nexaworks.rafiq.service.notification;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;

import io.sentry.Sentry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service("push")
@Slf4j
@RequiredArgsConstructor
public class PushNotificationService implements NotificationService<PushNotification> {
    @Override
    @Retryable(maxAttempts = 3, retryFor = {
            FirebaseMessagingException.class}, backoff = @Backoff(delay = 10000))
    public void sendNotification(PushNotification notificationDetails)
            throws FirebaseMessagingException {
        log.info("Sending SMS notification");
        try {
            Message message = Message.builder().setToken(notificationDetails.ft())
                    .setNotification(
                            Notification.builder().setTitle(notificationDetails.action().getTitle())
                                    .setBody(notificationDetails.body()).build())
                    .putAllData(notificationDetails.data()).build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Push notification response: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send push notification: {}", e.getMessage());
            throw e;
        }
    }
    @Recover
    public void recover(FirebaseMessagingException e, PushNotification notificationDetails) {
        log.error(
                "All retry attempts exhausted for push notification. Action: '{}', Token: '{}', Reason: {}",
                notificationDetails.action(), maskToken(notificationDetails.ft()), e.getMessage());
        Sentry.captureException(e);
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8)
            return "***";
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}
