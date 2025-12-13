package com.nexaworks.rafiq.medication.api.dto.request;

import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.MedicineStatus;
import com.nexaworks.rafiq.medication.entity.enums.MedicineType;

public record MedicineFilter(String search, MedicineStatus status, UUID groupId,
        MedicineType type) {
}
