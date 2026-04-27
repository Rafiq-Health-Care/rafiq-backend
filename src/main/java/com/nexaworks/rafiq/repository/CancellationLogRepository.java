package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.CancellationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CancellationLogRepository extends JpaRepository<CancellationLog, UUID> {
}
