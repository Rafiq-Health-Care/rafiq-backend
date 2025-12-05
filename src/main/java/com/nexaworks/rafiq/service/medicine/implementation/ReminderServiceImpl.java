package com.nexaworks.rafiq.service.medicine.implementation;

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
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.entities.ReminderLog;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;
import com.nexaworks.rafiq.exception.custom.ReminderNotFound;
import com.nexaworks.rafiq.repository.ReminderLogRepository;
import com.nexaworks.rafiq.repository.ReminderRepository;
import com.nexaworks.rafiq.service.medicine.ReminderService;
import com.nexaworks.rafiq.service.patient.PatientService;
import com.nexaworks.rafiq.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderServiceImpl implements ReminderService {
    private final ReminderRepository reminderRepository;
    private final PatientService patientService;
    private final ApplicationEventPublisher eventPublisher;
    private final ReminderLogRepository reminderLogRepository;
    private final UserService userService;

    @Override
    @Transactional
    public Reminder createReminder(Reminder reminder) {
        Patient patient = patientService.getPatientProfile();
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
        UUID patientId = userService.getUserId();
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

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ReminderNotFound("Reminder not found"));
        UUID patientId = userService.getUserId();

        if (!reminder.getPatient().getId().equals(patientId)) {
            throw new ReminderNotFound("Invalid Reminder Id");
        }
        ReminderLog reminderLog = ReminderLog.builder().status(status).reminder(reminder)
                .patient(reminder.getPatient()).timestamp(takenTime).build();
        reminderLogRepository.save(reminderLog);
        // todo update the next reminder date
    }

    @Override
    public Page<GetAllRemindersResponse> getAllReminders(Pageable pageable) {
        Patient patient = patientService.getPatientProfile();
        return reminderRepository.findAll(patient.getId(), pageable);
    }

    @Override
    public Reminder getReminderById(UUID reminderId) {
        return getReminder(reminderId);
    }

    private @NotNull Reminder getReminder(UUID reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ReminderNotFound("Reminder not found"));
        if (!reminder.getPatient().getId().equals(userService.getUserId())) {
            throw new ReminderNotFound("Invalid Reminder Id");
        }
        return reminder;
    }

    @Override
    @Transactional
    public Reminder updateVibration(UUID reminderId, Boolean vibrate) {
        Reminder reminder = getReminder(reminderId);
        reminder.setVibrate(vibrate);
        return reminderRepository.save(reminder);
    }

    @Override
    @Transactional
    public void deleteReminder(UUID reminderId) {
        Reminder reminder = getReminder(reminderId);
        reminder.getMedicine().setReminder(null);
        reminderRepository.delete(reminder);
    }

    @Override
    @Transactional
    public void disableReminder(UUID reminderId, Boolean disable) {
        Reminder reminder = getReminder(reminderId);
        reminder.setDisable(disable);
    }

}
