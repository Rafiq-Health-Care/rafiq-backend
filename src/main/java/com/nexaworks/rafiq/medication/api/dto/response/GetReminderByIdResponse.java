package com.nexaworks.rafiq.medication.api.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.Day;
import com.nexaworks.rafiq.medication.entity.enums.MedicineFrequency;
import com.nexaworks.rafiq.medication.entity.enums.ReminderFrequency;

public record GetReminderByIdResponse(String dosage, String medicineName, String notes,
        LocalDateTime nextDosage, UUID medicineId, boolean vibrate, MedicineFrequency frequency,
        ReminderFrequency reminderFrequency, List<Day> customDays) {

}
