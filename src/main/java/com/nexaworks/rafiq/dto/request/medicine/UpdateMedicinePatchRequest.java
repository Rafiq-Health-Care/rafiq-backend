package com.nexaworks.rafiq.dto.request.medicine;

import java.time.Instant;
import java.util.Optional;

import com.nexaworks.rafiq.entities.enums.MedicineFrequency;
import com.nexaworks.rafiq.entities.enums.MedicineStatus;
import com.nexaworks.rafiq.entities.enums.MedicineType;

public record UpdateMedicinePatchRequest(Optional<String> name, Optional<String> dosage,
        Optional<String> notes, Optional<MedicineFrequency> frequency, Optional<Instant> startDate,
        Optional<Instant> endDate, Optional<MedicineType> type, Optional<MedicineStatus> status) {
}
