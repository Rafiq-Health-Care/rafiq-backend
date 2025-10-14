package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Lab;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LabRepository extends JpaRepository<Lab, UUID> {
}
