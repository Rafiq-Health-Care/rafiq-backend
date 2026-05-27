package com.nexaworks.rafiq.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexaworks.rafiq.dto.response.doctor.DoctorStatsDTO;
import com.nexaworks.rafiq.entities.Doctor;

import jakarta.persistence.LockModeType;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    void findByIdWithLock(UUID id);

    Page<Doctor> findAll(Specification<Doctor> search, Pageable pageable);

    @Modifying
    @Query("UPDATE Doctor d SET d.biography = :biography WHERE d.id = :userId")
    void updateBiography(@Param("userId") UUID userId, @Param("biography") String biography);

    @Query("""
                SELECT d
                FROM Doctor d
                LEFT JOIN  d.subSpecializations
                WHERE d.id = :id
            """)
    Optional<Doctor> findProfileById(UUID id);

    @Query("""
                SELECT new com.nexaworks.rafiq.dto.response.doctor.DoctorStatsDTO(
                    (SELECT MIN(cs.startTime)
                     FROM ConsultationSlot cs
                     WHERE cs.doctor.id = :id
                       AND cs.status = com.nexaworks.rafiq.entities.enums.SlotStatus.AVAILABLE),
                    (SELECT COUNT(c.id)
                     FROM Consultation c
                     WHERE c.doctor.id = :id
                       AND c.status = com.nexaworks.rafiq.entities.enums.ConsultationStatus.COMPLETED)
                )
                FROM Doctor d
                WHERE d.id = :id
            """)
    Optional<DoctorStatsDTO> findStatsByDoctorId(UUID id);
}
