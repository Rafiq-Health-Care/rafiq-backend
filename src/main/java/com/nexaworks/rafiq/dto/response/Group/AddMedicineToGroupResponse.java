package com.nexaworks.rafiq.dto.response.Group;

import java.util.UUID;

public record AddMedicineToGroupResponse(UUID groupId, int addedCount) {
}
