package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexaworks.rafiq.entities.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id")
    void markNotificationAsRead(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId")
    void markAllAsReadByUserId(@Param("userId") UUID userId);

    Page<Notification> getAllByUserId(UUID userId, Pageable pageable);
}
