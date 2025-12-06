package com.nexaworks.rafiq.lab.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.lab.entity.Lab;

public interface LabRepository extends JpaRepository<Lab, UUID> {
    Optional<Lab> findLabByNameContainsIgnoreCase(String s);
}
