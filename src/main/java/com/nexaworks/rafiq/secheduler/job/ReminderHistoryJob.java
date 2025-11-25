package com.nexaworks.rafiq.secheduler.job;

import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.event.MedicineNotification;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.entities.ReminderLog;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;
import com.nexaworks.rafiq.repository.ReminderLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderHistoryJob implements Job {
    private final ReminderLogRepository reminderLogRepository;
    private final ApplicationEventPublisher publisher;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Update reminder state");
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        UUID reminderLogId = UUID.fromString(jobDetail.getJobDataMap().getString("reminderLogId"));
        String notificationToken = jobDetail.getJobDataMap().getString("notificationToken");
        ReminderLog reminderLog = reminderLogRepository.findById(reminderLogId).orElseThrow();
        if (reminderLog.getStatus() == ReminderStatus.SERVED) {
            reminderLog.setStatus(ReminderStatus.MISSED);
            reminderLogRepository.save(reminderLog);

        } else if (reminderLog.getStatus() == ReminderStatus.SNOOZED) {
            reminderLog.setStatus(ReminderStatus.SERVED);
            reminderLogRepository.save(reminderLog);
            Reminder reminder = reminderLog.getReminder();
            Medicine medicine = reminder.getMedicine();
            MedicineNotification notification = new MedicineNotification(notificationToken,
                    medicine.getName(), medicine.getId(), reminder.isVibrate(),
                    medicine.getDosage(), medicine.getNotes(), reminderLog.getId());
            publisher.publishEvent(notification);
        }
    }
}
