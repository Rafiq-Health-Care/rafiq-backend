package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID>, JpaSpecificationExecutor<Consultation> {
    @Query("""
    SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
    FROM Consultation c
    WHERE c.doctor.id = :doctorId
    AND c.status != :status
    AND c.timeSlot.startTime < :end
    AND c.timeSlot.endTime > :start
    """)
    boolean existsByOverlapping(
            @Param("start") LocalDateTime startTime,
            @Param("end") LocalDateTime endTime,
            @Param("doctorId") UUID doctorId,
            @Param("status") ConsultationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Consultation> findConsultationById(UUID id);

    @Query("""
    SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
    FROM Consultation c
    WHERE c.doctor.id = :doctorId
    AND c.id != :consultationId
    AND c.status != :status
    AND c.timeSlot.startTime < :end
    AND c.timeSlot.endTime > :start
    """)
    boolean existsByOverlapping(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("doctorId") UUID userId,
                                @Param("consultationId") UUID id,
                                @Param("status") ConsultationStatus status);
}
