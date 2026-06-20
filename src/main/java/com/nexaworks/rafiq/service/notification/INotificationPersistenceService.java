package com.nexaworks.rafiq.service.notification;

import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.notification.NotificationResponse;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;

public interface INotificationPersistenceService {
    void saveNotification(PushNotification notification, User user);

    void markNotificationAsRead(UUID id);

    void deleteNotification(UUID id);

    void markAllAsRead();

    PageResponse<NotificationResponse> getAll(Pageable pageable);

    NotificationResponse getById(UUID id);
}
