package com.nexaworks.rafiq.repository;

import com.nexaworks.rafiq.entities.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.UUID;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
    @Query("""
    SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
    FROM Consultation c
    WHERE c.doctor.id = :doctorId
    AND c.timeSlot.startTime < :end
    AND c.timeSlot.endTime > :start
    """)
    boolean existsByOverlapping(
            @Param("start") LocalTime startTime,
            @Param("end") LocalTime endTime,
            @Param("doctorId") UUID doctorId
    );
}
