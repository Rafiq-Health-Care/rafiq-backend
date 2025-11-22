package com.nexaworks.rafiq.dto.response.medicine;

import java.util.UUID;

import com.nexaworks.rafiq.enums.MedicineFrequency;
import com.nexaworks.rafiq.enums.MedicineStatus;

public record MedicineGroupResponse(UUID id, String name, String dosage,
        MedicineFrequency frequency, MedicineStatus Status) {
}
