package com.nexaworks.rafiq.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.service.consultation.ConsultationPreparationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Qualifier("jobRunr")
@Slf4j
@RequiredArgsConstructor
public class JobRunrPreparationScheduler implements PreparationScheduler {
    private final ConsultationPreparationService consultationPreparationService;
    @Override
    public void schedulePreparation(UUID consultationId, LocalDateTime startTime) {
        log.info("Scheduling preparation for consultation {} at {}", consultationId, startTime);
        BackgroundJob.schedule(consultationId, startTime.minusMinutes(5),
                () -> consultationPreparationService.prepare(consultationId));

    }
}
