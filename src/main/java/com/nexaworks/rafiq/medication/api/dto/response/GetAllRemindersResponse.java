package com.nexaworks.rafiq.medication.api.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GetAllRemindersResponse {
    UUID getReminderId();
    LocalDateTime getTime();
    String getDosage();
    String getMedicineName();
    UUID getMedicineId();
}
