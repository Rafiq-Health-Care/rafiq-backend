package com.nexaworks.rafiq.labTest.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.labTest.entity.LabResult;

public interface LabResultRepository extends JpaRepository<LabResult, UUID> {
}
