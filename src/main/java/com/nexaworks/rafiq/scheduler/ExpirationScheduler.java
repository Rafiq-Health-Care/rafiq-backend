package com.nexaworks.rafiq.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ExpirationScheduler {
    void scheduleConsultationSlotExpiration(UUID consultationSlotId, LocalDateTime endTime);
    void deleteExpirationJob(UUID consultationSlotId);

    void reSchedule(UUID id, LocalDateTime endTime);
}
