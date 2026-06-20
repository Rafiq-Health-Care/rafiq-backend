package com.nexaworks.rafiq.scheduler;

import java.time.LocalDateTime;

import com.nexaworks.rafiq.entities.Consultation;

public interface PreparationScheduler {
    void scheduleReminder(Consultation consultation, LocalDateTime startTime);
}
