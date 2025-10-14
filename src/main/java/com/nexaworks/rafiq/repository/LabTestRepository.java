package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LabTestRepository extends JpaRepository<LabTest, UUID> {
}
