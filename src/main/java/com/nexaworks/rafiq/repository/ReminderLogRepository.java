package com.nexaworks.rafiq.repository;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexaworks.rafiq.dto.request.reminder.GetAllRemindersHistoryResponseProjection;
import com.nexaworks.rafiq.entities.ReminderLog;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;

public interface ReminderLogRepository extends JpaRepository<ReminderLog, UUID> {

    @Query("""
            SELECT m.id AS medicineId,
                   m.name AS medicineName,
                   m.dosage AS dosage,
                   rl.timestamp AS time
            FROM ReminderLog rl
            JOIN Reminder r ON rl.reminder
            JOIN Medicine m ON r.medicine
            WHERE (CAST(:startDate AS timestamp) IS NULL OR rl.updatedAt >= :startDate)
              AND (CAST(:endDate AS timestamp) IS NULL OR rl.updatedAt <= :endDate)
              AND (CAST(:reminderId AS uuid) IS NULL OR r.id = :reminderId)
              AND (:status IS NULL OR rl.status = :status)
              AND (CAST(:patientId AS uuid) IS NULL OR r.patient.id = :patientId)
            """)
    Page<GetAllRemindersHistoryResponseProjection> findLogsHistory(
            @Param("startDate") Instant startDate, @Param("endDate") Instant endDate,
            @Param("reminderId") UUID reminderId, @Param("status") ReminderStatus status,
            @Param("patientId") UUID patientId, Pageable pageable);
}
