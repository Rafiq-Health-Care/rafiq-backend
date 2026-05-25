package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.MessageLog;

public interface MessageLogRepository extends JpaRepository<MessageLog, UUID> {
}
