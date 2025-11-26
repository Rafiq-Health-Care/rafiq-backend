package com.nexaworks.rafiq.secheduler.service.serviceImpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.secheduler.job.MedicineReminderJob;
import com.nexaworks.rafiq.secheduler.job.ReminderHistoryJob;
import com.nexaworks.rafiq.secheduler.service.QuartzSchedulerService;
import com.nexaworks.rafiq.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuartzSchedulerServiceImpl implements QuartzSchedulerService {
    private final Scheduler scheduler;
    private final UserService userService;

    @Override
    @Transactional
    public void scheduleJob(String jobName, String groupName, String cronExpression,
            Map<String, String> jobData) throws SchedulerException {
        if (jobName == null || jobName.trim().isEmpty()) {
            throw new IllegalArgumentException("Job name cannot be empty");
        }
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("Cron expression cannot be empty");
        }
        if (jobData == null) {
            throw new IllegalArgumentException("Job data cannot be null");
        }
        if (scheduler.checkExists(JobKey.jobKey(jobName, groupName))) {
            log.warn("Job already exists: {} in group: {}", jobName, groupName);
            return;
        }
        log.info("Scheduling job: {} in group: {} with cron: {}", jobName, groupName,
                cronExpression);

        try {
            JobDetail jobDetail = JobBuilder.newJob(MedicineReminderJob.class)
                    .withIdentity(jobName, groupName).storeDurably().build();
            jobDetail.getJobDataMap().putAll(jobData);

            String triggerName = jobName + "Trigger";
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression)
                    .withMisfireHandlingInstructionDoNothing();

            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, groupName)
                    .withSchedule(scheduleBuilder).startNow().build();
            scheduler.scheduleJob(jobDetail, trigger);

            log.info("Job scheduled: {} in group: {} with cron: {}", jobName, groupName,
                    cronExpression);
        } catch (Exception e) {
            log.error("Error scheduling job: {}", jobName, e);
            throw new SchedulerException("Error scheduling job: " + jobName, e);

        }
    }

    @Override
    public void deleteJob(JobKey jobKey) throws SchedulerException {
        log.info("Deleting job: {}", jobKey);
        if (!scheduler.checkExists(jobKey)) {
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            triggers.forEach(trigger -> {
                try {
                    scheduler.unscheduleJob(trigger.getKey());
                } catch (SchedulerException e) {
                    log.error("Error unscheduling trigger: {}", trigger.getKey(), e);
                }
            });
            scheduler.deleteJob(jobKey);
            log.info("Job deleted: {} from scheduler", jobKey);

        } else {
            log.info("Job not found: {}", jobKey);
        }
    }

    @Override
    public void updateJob(JobKey medicineReminder, String cornExpression)
            throws SchedulerException {
        if (cornExpression == null || cornExpression.trim().isEmpty()) {
            throw new IllegalArgumentException("Cron expression cannot be empty");
        }
        if (!scheduler.checkExists(medicineReminder)) {
            log.warn("Job not found: {}", medicineReminder);
            return;
        }
        log.info("Updating job: {} with cron: {}", medicineReminder, cornExpression);
        JobDetail jobDetail = scheduler.getJobDetail(medicineReminder);
        if (jobDetail == null) {
            log.warn("Job not found: {}", medicineReminder);
            throw new SchedulerException("Job not found: " + medicineReminder);
        }
        List<? extends Trigger> triggers = scheduler.getTriggersOfJob(medicineReminder);
        triggers.forEach(trigger -> {
            try {
                scheduler.unscheduleJob(trigger.getKey());
            } catch (SchedulerException e) {
                throw new RuntimeException(e);
            }
        });
        String triggerName = medicineReminder.getName() + "Trigger";
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cornExpression)
                .withMisfireHandlingInstructionDoNothing();
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerName, medicineReminder.getGroup())
                .withSchedule(scheduleBuilder).startNow().build();
        scheduler.scheduleJob(jobDetail, trigger);
    }

    @Override
    public void scheduleOneTimeJob(Map<String, String> reminderLogData, String groupName,
            LocalDateTime dateTime) throws SchedulerException {
        try {
            JobDetail jobDetail = JobBuilder.newJob(ReminderHistoryJob.class)
                    .withIdentity("ReminderHistoryJob-" + UUID.randomUUID(), groupName)
                    .storeDurably().build();
            jobDetail.getJobDataMap().putAll(reminderLogData);

            String triggerName = "ReminderHistoryJobTrigger-" + UUID.randomUUID();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .startAt(Date
                            .from(dateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                    .withIdentity(triggerName, groupName).build();
            scheduler.scheduleJob(jobDetail, trigger);
            log.info("One-time job scheduled: {} in group: {} at {}", jobDetail.getKey(), groupName,
                    dateTime);

        } catch (SchedulerException e) {
            throw new SchedulerException("Error scheduling one-time job", e);
        }

    }

}
