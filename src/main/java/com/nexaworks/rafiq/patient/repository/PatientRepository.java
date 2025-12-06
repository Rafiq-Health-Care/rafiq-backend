package com.nexaworks.rafiq.patient.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.patient.entity.model.Patient;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
}
