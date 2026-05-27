package com.nexaworks.rafiq.service.notification;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.notification.NotificationResponse;
import com.nexaworks.rafiq.entities.Notification;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.mapper.NotificationMapper;
import com.nexaworks.rafiq.rabbit.notificaiton.PushNotification;
import com.nexaworks.rafiq.repository.NotificationRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationPersistenceService implements INotificationPersistenceService {
    private final NotificationRepository notificationRepository;
    private final AuthService authService;
    private final NotificationMapper mapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveNotification(PushNotification notification, User user) {
        log.info("Saving notification {}", notification);
        Notification savedNotification = Notification.builder().id(notification.notificationId())
                .user(user).title(notification.action().getTitle()).message(notification.body())
                .data(notification.data()).build();
        notificationRepository.save(savedNotification);
        log.info("Notification saved successfully");
    }

    @Override
    public void markNotificationAsRead(UUID id) {
        notificationRepository.markNotificationAsRead(id);
    }

    @Override
    public void deleteNotification(UUID id) {
        notificationRepository.deleteById(id);
    }

    @Override
    public void markAllAsRead() {
        UUID userId = authService.getAuthenticateUserId();
        notificationRepository.markAllAsReadByUserId(userId);

    }

    @Override
    public PageResponse<NotificationResponse> getAll(Pageable pageable) {
        UUID userId = authService.getAuthenticateUserId();
        Page<Notification> notifications = notificationRepository.getAllByUserId(userId, pageable);
        return PageResponse.of(notifications, mapper::toResponse);
    }

    @Override
    public NotificationResponse getById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("Notification not found"));
        return mapper.toResponse(notification);
    }
}
