package com.nexaworks.rafiq.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexaworks.rafiq.dto.response.consultation.CallResponse;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

import jakarta.persistence.LockModeType;

public interface ConsultationRepository
        extends
            JpaRepository<Consultation, UUID>,
            JpaSpecificationExecutor<Consultation> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Consultation> findConsultationById(UUID id);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Consultation c
            WHERE c.patient.id = :patientId
            AND c.status != ConsultationStatus.CANCELLED
            AND c.slot.startTime < :end
            AND c.slot.endTime > :start
            """)
    boolean existsByPatientOverlapping(@Param("start") LocalDateTime startTime,
            @Param("end") LocalDateTime endTime, @Param("patientId") UUID id1);

    @Query("SELECT new com.nexaworks.rafiq.dto.response.consultation.CallResponse(c.id,c.accessToken) FROM Consultation c")
    CallResponse getConsultationCallInfo(UUID id);

    @Query("SELECT c FROM Consultation c WHERE c.patient.id = :patientId AND c.status= :status1")
    Page<Consultation> findAllByPatientIdAndStatus(@Param("patientId") UUID patientId,
            @Param("status1") ConsultationStatus status1, Pageable pageable);

    @Query("SELECT c FROM Consultation c WHERE c.patient.id = :patientId AND c.status= :status")
    List<Consultation> findAllPatientConsultation(@Param("patientId") UUID authenticateUserId,
            @Param("status") ConsultationStatus status);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Consultation c
            WHERE c.slot.doctor.id = :doctorId
            AND c.patient.id = :patientId
            AND c.status NOT IN :excludedStatuses
            """)
    boolean existsByDoctorAndPatientAndStatusNotIn(@Param("doctorId") UUID doctorId,
            @Param("patientId") UUID patientId,
            @Param("excludedStatuses") Collection<ConsultationStatus> excludedStatuses);

    Optional<Consultation> findConsultationByIdAndPatientId(UUID id, UUID patientId);
}
