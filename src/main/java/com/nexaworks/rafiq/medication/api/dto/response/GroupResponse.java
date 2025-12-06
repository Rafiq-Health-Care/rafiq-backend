package com.nexaworks.rafiq.medication.api.dto.response;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.medication.entity.enums.Color;

public record GroupResponse(UUID id, String name, String description, Color color, String iconUrl,
        int medicineCount, List<MedicinePreview> medicinePreviews) {
}
