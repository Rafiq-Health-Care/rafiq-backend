package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Specialization;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecializationRepository extends JpaRepository<Specialization, UUID> {
    boolean existsByCode(String code);

    Optional<Specialization> findByCode(String code);

    Optional<Specialization> findByName(String name);
}
