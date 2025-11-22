package com.nexaworks.rafiq.dto.response.Group;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.enums.Color;

public record GroupResponse(UUID id, String name, String description, Color color, String iconUrl,
        int medicineCount, List<MedicinePreview> medicinePreviews) {
}
