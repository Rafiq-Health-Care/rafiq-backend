package com.nexaworks.rafiq.dto.response.Group;

import java.time.Instant;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.Color;

public record AddGroupResponse(UUID groupId, UUID patientId, String description, Color color,
        String name, Instant createdAt, Instant updatedAt, String iconUrl, int medicineCount) {
}
