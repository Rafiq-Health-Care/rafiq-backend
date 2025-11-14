package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.PatientProfile;

public interface PatientRepository extends JpaRepository<PatientProfile, UUID> {
}
