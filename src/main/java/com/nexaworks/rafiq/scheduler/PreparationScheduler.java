package com.nexaworks.rafiq.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PreparationScheduler {
    void scheduleReminder(UUID consultationId, String fcm, LocalDateTime startTime);
}
