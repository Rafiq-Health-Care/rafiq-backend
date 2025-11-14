package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.DoctorProfile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<DoctorProfile, UUID> {}
