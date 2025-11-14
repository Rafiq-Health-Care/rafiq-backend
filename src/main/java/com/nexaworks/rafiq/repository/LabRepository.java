package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Lab;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabRepository extends JpaRepository<Lab, UUID> {
    Optional<Lab> findLabByNameContainsIgnoreCase(String s);
}
