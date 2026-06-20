package com.nexaworks.rafiq.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexaworks.rafiq.entities.Payout;
import com.nexaworks.rafiq.entities.enums.PayoutStatus;

public interface PayoutRepository extends JpaRepository<Payout, UUID> {
    Optional<Payout> findByConsultationId(UUID consultationId);

    List<Payout> findByStatusAndReleaseAtBefore(PayoutStatus status, Instant releaseAt);

    List<Payout> findByDoctorIdAndStatus(UUID doctorId, PayoutStatus status);

    @Query("SELECT p FROM Payout p WHERE p.status = :status AND p.releaseAt <= :now ORDER BY p.releaseAt ASC")
    List<Payout> findPayoutsReadyForProcessing(@Param("status") PayoutStatus status,
            @Param("now") Instant now);
}
