package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientRepository extends JpaRepository<PatientProfile , UUID> {
}
