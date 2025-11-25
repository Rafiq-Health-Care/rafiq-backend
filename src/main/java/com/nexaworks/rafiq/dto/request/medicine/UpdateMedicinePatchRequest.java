package com.nexaworks.rafiq.dto.request.medicine;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import com.nexaworks.rafiq.entities.enums.*;

public record UpdateMedicinePatchRequest(Optional<String> name, Optional<String> dosage,
        Optional<String> notes, Optional<MedicineFrequency> frequency, Optional<Instant> startDate,
        Optional<Instant> endDate, Optional<MedicineType> type, Optional<MedicineStatus> status,
        Optional<ReminderFrequency> reminderFrequency, Optional<List<Day>> customDays) {
}
