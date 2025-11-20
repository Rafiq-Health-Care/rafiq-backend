package com.nexaworks.rafiq.dto.request.medicine;

import java.time.Instant;

import com.nexaworks.rafiq.enums.MedicineFrequency;
import com.nexaworks.rafiq.enums.MedicineStatus;
import com.nexaworks.rafiq.enums.MedicineType;

public record UpdateMedicineRequest(String name, String dosage, String notes,
        MedicineFrequency frequency, Instant startDate, Instant endDate, MedicineType type,
        MedicineStatus status) {
}
