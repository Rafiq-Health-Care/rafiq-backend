package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.LabResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LabResultRepository extends JpaRepository<LabResult, UUID> {
}
