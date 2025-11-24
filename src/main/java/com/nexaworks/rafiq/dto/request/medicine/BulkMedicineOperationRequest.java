package com.nexaworks.rafiq.dto.request.medicine;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.Action;

import jakarta.validation.constraints.NotNull;

public record BulkMedicineOperationRequest(@NotNull List<UUID> medicineIds, Action action,
        Optional<UUID> groupId) {
}
