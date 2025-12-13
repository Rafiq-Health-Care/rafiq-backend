package com.nexaworks.rafiq.medication.api.dto.response;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bulk operation response")
public record BulkOperationResponse(@Schema int successCount, @Schema List<UUID> failedIds) {
}
