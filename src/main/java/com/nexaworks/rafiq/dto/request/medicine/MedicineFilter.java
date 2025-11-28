package com.nexaworks.rafiq.dto.request.medicine;

import com.nexaworks.rafiq.entities.enums.MedicineStatus;
import com.nexaworks.rafiq.entities.enums.MedicineType;

public record MedicineFilter(String search, MedicineStatus status, String groupId,
        MedicineType type) {
}
