package com.nexaworks.rafiq.dto.response.reminder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.Day;
import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.ReminderFrequency;

public record GetReminderByIdResponse(String dosage, String medicineName, String notes,
        LocalDateTime nextDosage, UUID medicineId, boolean vibrate, MedicineFrequency frequency,
        ReminderFrequency reminderFrequency, List<Day> customDays) {

}
