package com.nexaworks.rafiq.doctor.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexaworks.rafiq.doctor.entity.model.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
}
