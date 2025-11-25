package com.nexaworks.rafiq.secheduler.service;

import java.util.Map;

import org.quartz.JobKey;
import org.quartz.SchedulerException;

public interface QuartzSchedulerService {

    void scheduleJob(String jobName, String groupName, String cronExpression,
            Map<String, String> jobData) throws SchedulerException;

    void deleteJob(JobKey jobKey) throws SchedulerException;

    void updateJob(JobKey medicineReminder, String s) throws SchedulerException;
}
