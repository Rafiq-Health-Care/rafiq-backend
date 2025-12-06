package com.nexaworks.rafiq.doctor.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.doctor.entity.model.Specialization;

public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {
    Optional<Specialization> findByCode(String code);
}
