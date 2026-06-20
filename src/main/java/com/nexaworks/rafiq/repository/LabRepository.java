package com.nexaworks.rafiq.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.Lab;
@Deprecated
public interface LabRepository extends JpaRepository<Lab, UUID> {
    Optional<Lab> findLabByNameContainsIgnoreCase(String s);
}
