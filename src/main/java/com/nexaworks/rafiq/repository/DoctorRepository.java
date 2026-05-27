package com.nexaworks.rafiq.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"subSpecializations"})
    @Query("SELECT d FROM Doctor d WHERE d.id = :id")
    Optional<Doctor> findProfileInfoById(UUID id);

    @Query("""
                SELECT d
                FROM Doctor d
                LEFT JOIN FETCH d.subSpecializations
                LEFT JOIN FETCH d.education
                LEFT JOIN FETCH d.experience
                WHERE d.id = :id
            """)
    Optional<Doctor> findProfileById(UUID id);

    @Query("""
                SELECT new com.nexaworks.rafiq.dto.response.doctor.DoctorStatsDTO(
                    MIN(cs.startTime),
                    COUNT(DISTINCT c.id)
                )
                FROM Doctor d
                LEFT JOIN d.consultationSlots cs ON cs.startTime > CURRENT_TIMESTAMP
                LEFT JOIN d.consultations c ON c.status = com.nexaworks.rafiq.entities.enums.ConsultationStatus.COMPLETED
                WHERE d.id = :id
                GROUP BY d.id
            """)
    Optional<DoctorStatsDTO> findStatsByDoctorId(UUID id);
}
