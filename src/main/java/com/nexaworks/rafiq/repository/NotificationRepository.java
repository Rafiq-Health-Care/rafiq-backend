package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.Notification;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
