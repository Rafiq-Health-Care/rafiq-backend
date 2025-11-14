package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.LabTest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabTestRepository extends JpaRepository<LabTest, UUID> {
  Page<LabTest> findAllByPatientId(UUID id, Pageable pageable);
}
