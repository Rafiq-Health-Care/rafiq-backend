package com.nexaworks.rafiq.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.nexaworks.rafiq.entities.ConsultationSummary;

public interface ConsultationSummaryRepository
        extends
            JpaRepository<ConsultationSummary, UUID>,
            JpaSpecificationExecutor<ConsultationSummary> {

    Optional<ConsultationSummary> findByConsultationId(UUID consultationId);
}
