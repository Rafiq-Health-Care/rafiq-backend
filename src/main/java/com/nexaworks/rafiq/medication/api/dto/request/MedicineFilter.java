package com.nexaworks.rafiq.medication.api.dto.request;

import com.nexaworks.rafiq.medication.entity.enums.MedicineStatus;
import com.nexaworks.rafiq.medication.entity.enums.MedicineType;

public record MedicineFilter(String search, MedicineStatus status, String groupId,
        MedicineType type) {
}
