package com.nexaworks.rafiq.secheduler.job;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.quartz.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.event.MedicineNotification;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.entities.ReminderLog;
import com.nexaworks.rafiq.entities.enums.MedicineStatus;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;
import com.nexaworks.rafiq.repository.ReminderLogRepository;
import com.nexaworks.rafiq.repository.ReminderRepository;
import com.nexaworks.rafiq.secheduler.service.QuartzSchedulerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MedicineReminderJob implements Job {
    private final ReminderLogRepository reminderLogRepo;
    private final ReminderRepository reminderRepo;
    private final ApplicationEventPublisher publisher;
    private final QuartzSchedulerService schedulerService;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Sending the notification");
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        UUID reminderId = UUID.fromString(jobDetail.getJobDataMap().getString("reminderId"));
        String notificationToken = jobDetail.getJobDataMap().getString("notificationToken");

        Reminder reminder = reminderRepo.findById(reminderId).orElseThrow();
        LocalDateTime nextFireTime = LocalDateTime.ofInstant(
                jobExecutionContext.getNextFireTime().toInstant(), ZoneId.systemDefault());
        reminder.setNextReminder(nextFireTime);

        ReminderLog reminderLog = ReminderLog.builder().reminder(reminder)
                .status(ReminderStatus.SERVED).timestamp(jobExecutionContext.getFireTime()
                        .toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .build();
        Medicine medicine = reminder.getMedicine();
        reminderLog = reminderLogRepo.save(reminderLog);
        reminderRepo.save(reminder);
        if (medicine.getStatus() == MedicineStatus.ACTIVE) {

            MedicineNotification notification = new MedicineNotification(notificationToken,
                    medicine.getName(), medicine.getId(), reminder.isVibrate(),
                    medicine.getDosage(), medicine.getNotes(), reminderLog.getId());
            publisher.publishEvent(notification);
        } else {
            try {
                schedulerService.deleteJob(
                        new JobKey(jobDetail.getKey().getName(), jobDetail.getKey().getGroup()));
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
            log.info("Medicine is not active. Deleted scheduled job for medicine: {}",
                    medicine.getName());
        }
    }
}
