package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.LabTest;

public interface LabTestRepository extends JpaRepository<LabTest, UUID> {
    Page<LabTest> findAllByPatientId(UUID id, Pageable pageable);
}
