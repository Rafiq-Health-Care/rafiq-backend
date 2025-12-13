package com.nexaworks.rafiq.labTest.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.labTest.entity.LabTest;

public interface LabTestRepository extends JpaRepository<LabTest, UUID> {
    Page<LabTest> findAllByPatientId(UUID id, Pageable pageable);

    List<LabTest> findAllByPatientId(UUID patientId);
}
