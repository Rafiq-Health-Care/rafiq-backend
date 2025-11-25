package com.nexaworks.rafiq.secheduler.service;

import org.quartz.JobKey;
import org.quartz.SchedulerException;

import com.nexaworks.rafiq.entities.Reminder;

public interface QuartzSchedulerService {

    void scheduleJob(String jobName, String groupName, String cronExpression, Reminder reminder)
            throws SchedulerException;

    void deleteJob(JobKey jobKey) throws SchedulerException;

    void updateJob(JobKey medicineReminder, String s) throws SchedulerException;
}
