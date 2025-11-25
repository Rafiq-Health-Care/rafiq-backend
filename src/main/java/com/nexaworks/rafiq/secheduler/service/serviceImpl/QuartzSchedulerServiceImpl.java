package com.nexaworks.rafiq.secheduler.service.serviceImpl;

import java.util.List;

import org.quartz.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.repository.ReminderLogRepository;
import com.nexaworks.rafiq.secheduler.job.MedicineReminderJob;
import com.nexaworks.rafiq.secheduler.service.QuartzSchedulerService;
import com.nexaworks.rafiq.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuartzSchedulerServiceImpl implements QuartzSchedulerService {
    private final Scheduler scheduler;
    private final ReminderLogRepository reminderLogRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void scheduleJob(String jobName, String groupName, String cronExpression,
            Reminder reminder) throws SchedulerException {

        JobDetail jobDetail = JobBuilder.newJob(MedicineReminderJob.class)
                .withIdentity(jobName, groupName)
                .usingJobData("reminderId", String.valueOf(reminder.getId()))
                .usingJobData("notificationToken", userService.getNotificationToken())
                .storeDurably().build();

        String triggerName = jobName + "Trigger";
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression)
                .withMisfireHandlingInstructionDoNothing();

        Trigger trigger = TriggerBuilder.newTrigger().withIdentity(triggerName, groupName)
                .withSchedule(scheduleBuilder).startNow().build();
        scheduler.scheduleJob(jobDetail, trigger);

        log.info("Job scheduled: {} in group: {} with cron: {}", jobName, groupName,
                cronExpression);
    }

    @Override
    public void deleteJob(JobKey jobKey) throws SchedulerException {
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
}
