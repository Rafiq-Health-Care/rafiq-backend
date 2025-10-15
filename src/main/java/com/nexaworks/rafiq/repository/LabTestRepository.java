package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.LabTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LabTestRepository extends JpaRepository<LabTest, UUID> {
    Page<LabTest> findAllByPatientId(UUID id, Pageable pageable);
}
