package com.nexaworks.rafiq.patient.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.patient.entity.model.WeightHistory;

public interface WeightHistoryRepository extends JpaRepository<WeightHistory, UUID> {
}
