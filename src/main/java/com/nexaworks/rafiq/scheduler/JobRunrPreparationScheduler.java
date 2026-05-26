package com.nexaworks.rafiq.scheduler;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jobrunr.scheduling.BackgroundJob;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.rabbit.manager.ConsultationNotificationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component("jobRunr")
@Slf4j
@RequiredArgsConstructor
public class JobRunrPreparationScheduler implements PreparationScheduler {

    private final ConsultationNotificationManager notificationManager;

    @Override
    public void scheduleReminder(Consultation consultation, LocalDateTime startTime) {
        log.info("Scheduling reminders for consultation: {}", consultation.getId());

        if (startTime.isBefore(LocalDateTime.now())) {
            log.warn("Start time is in the past for consultation: {}, skipping all scheduling",
                    consultation.getId());
            return;
        }

        UUID reminderId = UUID.nameUUIDFromBytes((consultation.getId() + ".reminder").getBytes());
        UUID finalReminderId = UUID
                .nameUUIDFromBytes((consultation.getId() + ".final_reminder").getBytes());

        if (startTime.isAfter(LocalDateTime.now().plusMinutes(30))) {
            BackgroundJob.schedule(reminderId, startTime.minusMinutes(30),
                    () -> notificationManager.publishReminderNotification(consultation, startTime));
            log.info("30-min reminder scheduled for consultation: {} [jobId: {}]",
                    consultation.getId(), reminderId);
        } else {
            log.info("Consultation: {} starts in less than 30 minutes, skipping reminder",
                    consultation.getId());
        }

        BackgroundJob.schedule(finalReminderId, startTime.minusMinutes(5),
                () -> notificationManager.publishReminderNotification(consultation, startTime));
        log.info("5-min reminder scheduled for consultation: {} at {} [jobId: {}]",
                consultation.getId(), startTime.minusMinutes(5), finalReminderId);
    }
}