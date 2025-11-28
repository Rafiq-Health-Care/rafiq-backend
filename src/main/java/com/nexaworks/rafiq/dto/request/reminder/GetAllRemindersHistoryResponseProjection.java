package com.nexaworks.rafiq.dto.request.reminder;

import java.time.Instant;
import java.util.UUID;

public interface GetAllRemindersHistoryResponseProjection {

    UUID getMedicineId();
    String getMedicineName();
    String getDosage();
    Instant getTime();
}
