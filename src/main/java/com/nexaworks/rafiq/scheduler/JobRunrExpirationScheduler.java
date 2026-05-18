package com.nexaworks.rafiq.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.service.consultation.IConsultationSlotExpirationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JobRunrExpirationScheduler implements ExpirationScheduler {
    private final IConsultationSlotExpirationService consultationSlotExpirationService;
    @Override
    public void scheduleConsultationSlotExpiration(UUID consultationSlotId, LocalDateTime endTime) {
        log.info("Scheduling consultation slot expiration for slot: {}", consultationSlotId);
        BackgroundJob.schedule(consultationSlotId, endTime,
                consultationSlotExpirationService::expire);
    }

    @Override
    public void deleteExpirationJob(UUID consultationSlotId) {
        log.info("Deleting expiration job for slot: {}", consultationSlotId);
        BackgroundJob.delete(consultationSlotId);
    }

    @Override
    public void reSchedule(UUID id, LocalDateTime endTime) {
        log.info("Rescheduling expiration job for slot: {}", id);
        deleteExpirationJob(id);
        scheduleConsultationSlotExpiration(id, endTime);
    }
}
