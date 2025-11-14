package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.entities.DoctorProfile;

public interface DoctorRepository extends JpaRepository<DoctorProfile, UUID> {}
