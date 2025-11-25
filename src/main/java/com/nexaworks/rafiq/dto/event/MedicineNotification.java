package com.nexaworks.rafiq.dto.event;

import java.util.UUID;

public record MedicineNotification(String notificationToken, String medicineName, UUID medicineId,
        boolean vibrate, String dosage, String notes, UUID reminderId) {
}
