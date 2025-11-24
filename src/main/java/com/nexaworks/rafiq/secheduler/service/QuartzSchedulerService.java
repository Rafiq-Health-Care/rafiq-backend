package com.nexaworks.rafiq.secheduler.service;

import org.quartz.SchedulerException;

import com.nexaworks.rafiq.entities.Reminder;

public interface QuartzSchedulerService {

    void scheduleJob(String jobName, String groupName, String cronExpression, Reminder reminder)
            throws SchedulerException;
}
