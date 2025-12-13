package com.nexaworks.rafiq.medication.api.dto.request;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Add medicines to group request")
public record AddMedicinesToGroup(@Schema List<UUID> medicineIds) {
}
