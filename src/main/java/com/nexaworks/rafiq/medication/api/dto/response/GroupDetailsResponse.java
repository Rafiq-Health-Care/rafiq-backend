package com.nexaworks.rafiq.medication.api.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.Color;

public record GroupDetailsResponse(UUID id, UUID patientId, String name, String description,
        Color color, String iconUrl, int medicineCount, List<MedicineGroupResponse> medicines,
        Instant createdAt, Instant updatedAt) {
}
