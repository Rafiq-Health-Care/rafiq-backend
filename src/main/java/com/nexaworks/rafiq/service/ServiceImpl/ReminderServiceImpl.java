package com.nexaworks.rafiq.service.ServiceImpl;

import org.quartz.SchedulerException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.dto.event.ReminderEvent;
import com.nexaworks.rafiq.entities.Medicine;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.repository.ReminderRepository;
import com.nexaworks.rafiq.secheduler.service.CornExpressionBuilder;
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
    private final ApplicationEventPublisher eventPublisher;
    private final QuartzSchedulerService schedulerService;
    private final CornExpressionBuilder cornExpressionBuilder;
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

    @Override
    @Transactional
    public void scheduleReminder(Reminder reminder) {
        Medicine medicine = reminder.getMedicine();
        String cornExpression = cornExpressionBuilder.buildCornExpression(medicine.getFrequency(),
                medicine.getReminderFrequency(), medicine.getCustomDays(), reminder.getStartDate());
        try {
            schedulerService.scheduleJob(medicine.getId().toString(), "MedicineReminder",
                    cornExpression, reminder);
        } catch (SchedulerException e) {
            log.error("Error scheduling reminder for medicine: {}", medicine.getName(), e);
        }

    }
}
