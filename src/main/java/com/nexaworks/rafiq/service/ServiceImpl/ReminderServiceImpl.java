package com.nexaworks.rafiq.service.ServiceImpl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.nexaworks.rafiq.dto.event.ReminderEvent;
import com.nexaworks.rafiq.dto.request.reminder.GetAllRemindersHistoryResponseProjection;
import com.nexaworks.rafiq.dto.request.reminder.ReminderFilters;
import com.nexaworks.rafiq.dto.response.reminder.GetAllRemindersResponse;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.entities.ReminderLog;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;
import com.nexaworks.rafiq.repository.ReminderLogRepository;
import com.nexaworks.rafiq.repository.ReminderRepository;
import com.nexaworks.rafiq.service.PatientService;
import com.nexaworks.rafiq.service.ReminderService;
import com.nexaworks.rafiq.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderServiceImpl implements ReminderService {
    private final ReminderRepository reminderRepository;
    private final PatientService patientService;
    private final ApplicationEventPublisher eventPublisher;

    private final UserService userService;
    private final ReminderLogRepository reminderLogRepository;

    @Override
    @Transactional
    public Reminder createReminder(Reminder reminder) {
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
    @Transactional(readOnly = true)
    public Page<GetAllRemindersHistoryResponseProjection> getHistory(Pageable pageable,
            ReminderFilters filters) {
        UUID patientId = patientService.getPatientProfile().getId();
        UUID reminderId = null;
        if (filters.medicineId() != null) {
            reminderId = reminderRepository.findReminderByMedicineId(filters.medicineId());
        }
        return reminderLogRepository.findLogsHistory(filters.startDate(), filters.endDate(),
                reminderId, filters.status(), patientId, pageable);
    }

    @Override
    @Transactional
    public void updateReminderStatus(UUID reminderId, ReminderStatus status,
            LocalDateTime takenTime) {
        // todo handle exception
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new IllegalArgumentException("Reminder not found"));
        PatientProfile patient = patientService.getPatientProfile();
        // todo handle exception
        if (!reminder.getPatient().getId().equals(patient.getId())) {
            throw new IllegalArgumentException("Invalid Reminder Id");
        }
        ReminderLog reminderLog = ReminderLog.builder().status(status).reminder(reminder)
                .patient(patient).timestamp(takenTime).build();
        reminderLogRepository.save(reminderLog);
        // todo update the next reminder date
    }

    @Override
    public Page<GetAllRemindersResponse> getAllReminders(Pageable pageable) {
        PatientProfile patient = patientService.getPatientProfile();
        return reminderRepository.findAll(patient.getId(), pageable);
    }

    @Override
    public Reminder getReminderById(UUID reminderId) {
        return getReminder(reminderId);
    }

    private @NotNull Reminder getReminder(UUID reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(IllegalArgumentException::new);
        if (!reminder.getPatient().getId().equals(patientService.getPatientProfile().getId())) {
            throw new IllegalArgumentException("Invalid Reminder Id");
        }
        return reminder;
    }

    @Override
    public Reminder updateVibration(UUID reminderId, Boolean vibrate) {
        Reminder reminder = getReminder(reminderId);
        reminder.setVibrate(vibrate);
        return reminderRepository.save(reminder);
    }

}
