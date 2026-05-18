package com.nexaworks.rafiq.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.rabbit.manager.ConsultationNotificationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("jobRunr")
@Slf4j
@RequiredArgsConstructor
public class JobRunrPreparationScheduler implements PreparationScheduler {

    private final ConsultationNotificationManager notificationManager;

    @Override
    public void scheduleReminder(UUID consultationId, String fcm, LocalDateTime startTime) {
        log.info("Scheduling reminders for consultation: {}", consultationId);

        if (startTime.isBefore(LocalDateTime.now())) {
            log.warn("Start time is in the past for consultation: {}, skipping all scheduling",
                    consultationId);
            return;
        }

        if (fcm == null || fcm.isBlank()) {
            log.warn("FCM token is missing for consultation: {}, skipping push scheduling",
                    consultationId);
            return;
        }

        UUID reminderId = UUID.nameUUIDFromBytes((consultationId + ".reminder").getBytes());
        UUID finalReminderId = UUID
                .nameUUIDFromBytes((consultationId + ".final_reminder").getBytes());

        if (startTime.isAfter(LocalDateTime.now().plusMinutes(30))) {
            BackgroundJob.schedule(reminderId, startTime.minusMinutes(30), () -> notificationManager
                    .publishReminderNotification(consultationId, fcm, startTime));
            log.info("30-min reminder scheduled for consultation: {} [jobId: {}]", consultationId,
                    reminderId);
        } else {
            log.info("Consultation: {} starts in less than 30 minutes, skipping reminder",
                    consultationId);
        }

        BackgroundJob.schedule(finalReminderId, startTime.minusMinutes(5), () -> notificationManager
                .publishReminderNotification(consultationId, fcm, startTime));
        log.info("5-min reminder scheduled for consultation: {} at {} [jobId: {}]", consultationId,
                startTime.minusMinutes(5), finalReminderId);
    }
}