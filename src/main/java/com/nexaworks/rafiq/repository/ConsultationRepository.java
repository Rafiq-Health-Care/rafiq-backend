package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Consultation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID>, JpaSpecificationExecutor<Consultation> {
    @Query("""
    SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
    FROM Consultation c
    WHERE c.doctor.id = :doctorId
    AND c.status != 'CANCELED'
    AND c.timeSlot.startTime < :end
    AND c.timeSlot.endTime > :start
    """)
    boolean existsByOverlapping(
            @Param("start") LocalTime startTime,
            @Param("end") LocalTime endTime,
            @Param("doctorId") UUID doctorId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Consultation> findConsultationById(UUID id);

    @Query("""
    SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
    FROM Consultation c
    WHERE c.doctor.id = :doctorId
    AND c.id != :consultationId
    AND c.status != 'CANCELED'
    AND c.timeSlot.startTime < :end
    AND c.timeSlot.endTime > :start
    """)
    boolean existsByOverlapping(@Param("start") LocalTime start,@Param("end") LocalTime end,@Param("doctorId") UUID userId,@Param("consultationId") UUID id);
}
