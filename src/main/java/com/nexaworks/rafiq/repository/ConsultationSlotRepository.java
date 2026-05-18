package com.nexaworks.rafiq.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexaworks.rafiq.dto.response.consultation.DoctorConsultationResponse;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.entities.enums.SlotStatus;

import jakarta.persistence.LockModeType;

public interface ConsultationSlotRepository
        extends
            JpaRepository<ConsultationSlot, UUID>,
            JpaSpecificationExecutor<ConsultationSlot> {
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM ConsultationSlot c
            WHERE c.doctor.id = :doctorId
            AND c.status != :status
            AND c.startTime < :end
            AND c.endTime > :start
            """)
    boolean existsByOverlapping(LocalDateTime startTime, LocalDateTime endTime, UUID id,
            ConsultationStatus consultationStatus);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM ConsultationSlot c
            WHERE c.doctor.id = :doctorId
            AND c.id != :consultationId
            AND c.status != :status
            AND c.startTime < :end
            AND c.endTime > :start
            """)
    boolean existsByOverlapping(@Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end, @Param("doctorId") UUID userId,
            @Param("consultationId") UUID id, @Param("status") ConsultationStatus status);

    @Query("SELECT new com.nexaworks.rafiq.dto.response.consultation.DoctorConsultationResponse(c.id,c.startTime,c.endTime) FROM ConsultationSlot  c where c.doctor.id = :id AND c.status = :slotStatus")
    List<DoctorConsultationResponse> getDoctorAvailableConsultation(UUID id, SlotStatus slotStatus);

    @Query("SELECT c FROM ConsultationSlot c WHERE c.doctor.id = :doctorId AND c.status = :status")
    List<ConsultationSlot> findAllDoctorUpcoming(UUID doctorId, SlotStatus slotStatus);

    @Query("SELECT true FROM ConsultationSlot c WHERE c.id = :slotId AND c.status = SlotStatus.BOOKED")
    boolean isBooked(UUID slotId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ConsultationSlot c WHERE c.id = :id")
    Optional<ConsultationSlot> findConsultationByIdWithLock(UUID id);
}
