package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.PatientProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<PatientProfile, UUID> {}
