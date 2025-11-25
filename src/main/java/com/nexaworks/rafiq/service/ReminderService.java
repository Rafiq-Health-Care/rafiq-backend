package com.nexaworks.rafiq.service;

import org.quartz.SchedulerException;

import com.nexaworks.rafiq.entities.Reminder;

public interface ReminderService {
    Reminder createReminder(Reminder reminder) throws SchedulerException;

    void scheduleReminder(Reminder reminder);
}
