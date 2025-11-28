package com.nexaworks.rafiq.dto.response.medicine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.*;

public record MedicineResponse(UUID id, UUID patientId, String name, String dosage,
        MedicineFrequency frequency, ReminderFrequency reminderFrequency, List<Day> customDays,
        Instant startDate, Instant endDate, String notes, String photoUrl, MedicineType type,
        MedicineStatus status, UUID groupId, String groupName, UUID reminderId,
        LocalDateTime nextReminder, Instant createdAt, Instant updatedAt) {
}
