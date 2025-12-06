package com.nexaworks.rafiq.medication.api.dto.request;

import java.time.Instant;
import java.util.UUID;

public interface GetAllRemindersHistoryResponseProjection {

    UUID getMedicineId();
    String getMedicineName();
    String getDosage();
    Instant getTime();
}
