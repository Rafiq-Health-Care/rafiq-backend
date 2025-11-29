package com.nexaworks.rafiq.dto.response.medicine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.Day;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.MedicineStatus;
import com.nexaworks.rafiq.entities.enums.ReminderFrequency;

public record MedicineGroupResponse(UUID id, String name, String dosage,
        MedicineFrequency frequency, ReminderFrequency reminderFrequency, List<Day> customDays,
        MedicineStatus status, LocalDateTime nextReminder) {
}
