package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.RefundRequest;

public interface RefundRepository extends JpaRepository<RefundRequest, UUID> {
    boolean existsByConsultation(Consultation consultation);
}
