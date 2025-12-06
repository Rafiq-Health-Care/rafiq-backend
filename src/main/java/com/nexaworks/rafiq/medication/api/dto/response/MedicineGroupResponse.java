package com.nexaworks.rafiq.medication.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.Day;
import com.nexaworks.rafiq.medication.entity.enums.MedicineFrequency;
import com.nexaworks.rafiq.medication.entity.enums.MedicineStatus;
import com.nexaworks.rafiq.medication.entity.enums.ReminderFrequency;

public record MedicineGroupResponse(UUID id, String name, String dosage,
        MedicineFrequency frequency, ReminderFrequency reminderFrequency, List<Day> customDays,
        MedicineStatus status, LocalDateTime nextReminder) {
}
