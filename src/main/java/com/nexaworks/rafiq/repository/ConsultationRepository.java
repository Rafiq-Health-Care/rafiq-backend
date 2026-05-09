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

import com.nexaworks.rafiq.dto.response.consultation.CallResponse;
import com.nexaworks.rafiq.dto.response.consultation.DoctorConsultationResponse;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

import jakarta.persistence.LockModeType;

public interface ConsultationRepository
        extends
            JpaRepository<Consultation, UUID>,
            JpaSpecificationExecutor<Consultation> {
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Consultation c
            WHERE c.doctor.id = :doctorId
            AND c.status != :status
            AND c.timeSlot.startTime < :end
            AND c.timeSlot.endTime > :start
            """)
    boolean existsByOverlapping(@Param("start") LocalDateTime startTime,
            @Param("end") LocalDateTime endTime, @Param("doctorId") UUID doctorId,
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
            @Param("end") LocalDateTime end, @Param("doctorId") UUID userId,
            @Param("consultationId") UUID id, @Param("status") ConsultationStatus status);
    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM Consultation c
            WHERE c.doctor.id = :patientId
            AND c.status != :status
            AND c.timeSlot.startTime < :end
            AND c.timeSlot.endTime > :start
            """)
    boolean existsByPatientOverlapping(@Param("start") LocalDateTime startTime,
            @Param("end") LocalDateTime endTime, @Param("patientId") UUID id1,
            @Param("status") ConsultationStatus consultationStatus);

    @Query("SELECT new com.nexaworks.rafiq.dto.response.consultation.CallResponse(c.id,c.accessToken) FROM Consultation c")
    CallResponse getConsultationCallInfo(UUID id);

    @Query("SELECT c FROM Consultation c WHERE c.patient.id = :patientId AND c.status= :status1 OR c.status= :status2")
    List<Consultation> findAllByPatientIdAndStatus(@Param("patientId") UUID patientId,
            @Param("status1") ConsultationStatus status1,
            @Param("status2") ConsultationStatus status2);

    @Query("SELECT c FROM Consultation c WHERE c.doctor.id= :doctorId AND c.status= :status1 OR c.status= :status2")
    List<Consultation> findAllDoctorUpcoming(@Param("doctorId") UUID doctorId,
            @Param("status1") ConsultationStatus status1,
            @Param("status2") ConsultationStatus status2);

    @Query("SELECT c FROM Consultation c WHERE c.patient.id = :patientId AND c.status= :status")
    List<Consultation> findAllPatientConsultation(@Param("patientId") UUID authenticateUserId,
            @Param("status") ConsultationStatus status);

    @Query("SELECT new com.nexaworks.rafiq.dto.response.consultation.DoctorConsultationResponse(c.id,c.timeSlot.startTime,c.timeSlot.endTime) FROM Consultation  c where c.doctor.id = :id AND c.status = :status")
    List<DoctorConsultationResponse> getDoctorAvailableConsultation(@Param("id") UUID id,
            @Param("status") ConsultationStatus consultationStatus);
}
