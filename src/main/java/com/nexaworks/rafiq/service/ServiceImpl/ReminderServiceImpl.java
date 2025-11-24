package com.nexaworks.rafiq.service.ServiceImpl;

import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.entities.ReminderLog;
import com.nexaworks.rafiq.repository.ReminderRepository;
import com.nexaworks.rafiq.secheduler.service.QuartzSchedulerService;
import com.nexaworks.rafiq.service.PatientService;
import com.nexaworks.rafiq.service.ReminderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderServiceImpl implements ReminderService {
    private final ReminderRepository reminderRepository;
    private final PatientService patientService;
    private final QuartzSchedulerService schedulerService;
    @Override
    public Reminder createReminder(Reminder reminder) throws SchedulerException {
        PatientProfile patient = patientService.getPatientProfile();
        reminder.setPatient(patient);
        log.info("Creating reminder for medicine: {}", reminder.getMedicine().getName());
        ReminderLog reminderLog = ReminderLog.builder().reminder(reminder).build();
        // todo fire event
        return reminderRepository.save(reminder);
    }
}
