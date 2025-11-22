package com.nexaworks.rafiq.service.ServiceImpl;

import org.springframework.stereotype.Service;

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
    @Override
    public Reminder createReminder(Reminder reminder) {
        PatientProfile patient = patientService.getPatientProfile();
        reminder.setPatient(patient);
        log.info("Creating reminder for medicine: {}", reminder.getMedicine().getName());
        return reminderRepository.save(reminder);
    }
}
