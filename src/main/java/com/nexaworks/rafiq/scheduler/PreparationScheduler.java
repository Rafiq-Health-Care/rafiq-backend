package com.nexaworks.rafiq.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PreparationScheduler {
    void schedulePreparation(UUID consultationId, LocalDateTime startTime);
}
