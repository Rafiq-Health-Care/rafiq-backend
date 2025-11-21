package com.nexaworks.rafiq.dto.response.medicine;

import java.util.List;
import java.util.UUID;

public record BulkOperationResponse(int successCount, List<UUID> failedIds) {
}
