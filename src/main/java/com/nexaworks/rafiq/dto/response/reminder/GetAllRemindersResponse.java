package com.nexaworks.rafiq.dto.response.reminder;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GetAllRemindersResponse {
    UUID getReminderId();
    LocalDateTime getTime();
    String getDosage();
    String getMedicineName();
    UUID getMedicineId();
}
