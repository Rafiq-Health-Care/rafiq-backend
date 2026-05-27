package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.enums.Specialization;

import jakarta.persistence.LockModeType;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    void findByIdWithLock(UUID id);

    Page<Doctor> findBySpecialization(Specialization specialization, Pageable pageable);

    Page<Doctor> findAll(Specification<Doctor> search, Pageable pageable);

    @Modifying
    @Query("UPDATE Doctor d SET d.biography = :biography WHERE d.id = :userId")
    void updateBiography(@Param("userId") UUID userId, @Param("biography") String biography);
}
