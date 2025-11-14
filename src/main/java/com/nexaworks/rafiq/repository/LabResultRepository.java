package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.LabResult;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabResultRepository extends JpaRepository<LabResult, UUID> {}
