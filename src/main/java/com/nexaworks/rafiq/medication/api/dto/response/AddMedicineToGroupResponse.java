package com.nexaworks.rafiq.medication.api.dto.response;

import java.util.UUID;

public record AddMedicineToGroupResponse(UUID groupId, int addedCount) {
}
