package com.nexaworks.rafiq.medication.api.dto.request;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.Action;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Bulk medicine operation request")
public record BulkMedicineOperationRequest(@NotNull @Schema List<UUID> medicineIds,
        @Schema Action action, @Schema Optional<UUID> groupId) {
}
