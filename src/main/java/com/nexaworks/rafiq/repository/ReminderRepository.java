package com.nexaworks.rafiq.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nexaworks.rafiq.dto.response.reminder.GetAllRemindersResponse;
import com.nexaworks.rafiq.entities.Reminder;

public interface ReminderRepository extends JpaRepository<Reminder, UUID> {
    UUID findReminderByMedicineId(UUID medicineId);

    @Query("""
            SELECT m.id AS medicineId,
                   m.name AS medicineName,
                   m.dosage AS dosage,
                   r.id AS reminderId,
                   r.nextReminder AS time
            FROM  Reminder r JOIN r.medicine m
            WHERE r.patient.id = :id

            """)
    Page<GetAllRemindersResponse> findAll(UUID id, Pageable pageable);
}
