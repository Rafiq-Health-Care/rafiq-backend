package com.nexaworks.rafiq.service.notification;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Notification;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;
import com.nexaworks.rafiq.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationPersistenceService implements INotificationPersistenceService {
    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public void saveNotification(PushNotification notification, User user) {
        Notification savedNotification = Notification.builder().id(notification.notificationId())
                .user(user).title(notification.action().getTitle()).message(notification.body())
                .data(notification.data()).build();
        notificationRepository.save(savedNotification);
    }
}
