package com.nexaworks.rafiq.dto.response.Group;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.dto.response.medicine.MedicineGroupResponse;
import com.nexaworks.rafiq.entities.enums.Color;

public record GroupDetailsResponse(UUID id, UUID userId, String name, String description,
        Color color, String iconUrl, int medicineCount, List<MedicineGroupResponse> medicines,
        Instant createdAt, Instant updatedAt) {
}
