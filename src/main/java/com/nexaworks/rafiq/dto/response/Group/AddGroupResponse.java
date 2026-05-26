package com.nexaworks.rafiq.dto.response.Group;

import java.time.Instant;
import java.util.UUID;

public record AddGroupResponse(UUID groupId, UUID patientId, String description, String color,
        String name, Instant createdAt, Instant updatedAt, String iconUrl, int medicineCount) {
}
