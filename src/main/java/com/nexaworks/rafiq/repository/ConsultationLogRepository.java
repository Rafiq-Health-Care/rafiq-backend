package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.ConsultationLog;

public interface ConsultationLogRepository extends JpaRepository<ConsultationLog, UUID> {
}
