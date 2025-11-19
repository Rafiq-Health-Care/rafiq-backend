package com.nexaworks.rafiq.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.nexaworks.rafiq.enums.MedicineFrequency;
import com.nexaworks.rafiq.enums.MedicineStatus;
import com.nexaworks.rafiq.enums.MedicineType;

public record MedicineResponse(UUID id, UUID userId, String name, String dosage,
        MedicineFrequency frequency, Instant startDate, Instant endDate, String notes,
        String photoUrl, MedicineType type, MedicineStatus status, UUID groupId, String groupName,
        int reminderCount, Instant createdAt, Instant updatedAt) {
}
