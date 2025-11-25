package com.nexaworks.rafiq.service.ServiceImpl;

import org.quartz.SchedulerException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.dto.event.ReminderEvent;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.repository.ReminderRepository;
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
    private final ApplicationEventPublisher eventPublisher;
    @Override
    @Transactional
    public Reminder createReminder(Reminder reminder) throws SchedulerException {
        PatientProfile patient = patientService.getPatientProfile();
        reminder.setPatient(patient);
        log.info("Creating reminder for medicine: {}", reminder.getMedicine().getName());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new ReminderEvent(reminder));
            }
        });
        return reminderRepository.save(reminder);
    }
}
