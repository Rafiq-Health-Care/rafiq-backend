package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DoctorRepository extends JpaRepository<DoctorProfile, UUID> {
}
