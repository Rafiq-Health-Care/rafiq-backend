package com.nexaworks.rafiq.dto.response.reminder;

import java.time.LocalDateTime;
import java.util.UUID;

public record GetAllRemindersHistoryResponse(UUID medicineId, String medicineName, String dosage,
        LocalDateTime time) {
}
