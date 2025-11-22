package com.nexaworks.rafiq.dto.response.reminder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.enums.Day;
import com.nexaworks.rafiq.enums.MedicineStatus;
import com.nexaworks.rafiq.enums.ReminderFrequency;

public record AddReminderResponse(UUID id, UUID userId, UUID medicineId, String medicineName,
        String dosage, String time, ReminderFrequency frequency, List<Day> dayOfWeek,
        Instant startDate, Instant endDate, String notes, boolean vibrate, MedicineStatus status,
        Instant createdAt, Instant updatedAt) {
}
