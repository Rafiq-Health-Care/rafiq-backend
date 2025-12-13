package com.nexaworks.rafiq.medication.service.implementation;

import java.time.LocalDateTime;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.medication.api.dto.request.GetAllRemindersHistoryResponseProjection;
import com.nexaworks.rafiq.medication.api.dto.request.ReminderFilters;
import com.nexaworks.rafiq.medication.api.dto.response.GetAllRemindersResponse;
import com.nexaworks.rafiq.medication.entity.model.Reminder;
import com.nexaworks.rafiq.medication.entity.model.ReminderLog;
import com.nexaworks.rafiq.medication.entity.enums.ReminderStatus;
import com.nexaworks.rafiq.medication.exception.ReminderNotFound;
import com.nexaworks.rafiq.medication.repository.ReminderLogRepository;
import com.nexaworks.rafiq.medication.repository.ReminderRepository;
import com.nexaworks.rafiq.medication.service.ReminderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderServiceImpl implements ReminderService {
    private final ReminderRepository reminderRepository;
    private final ReminderLogRepository reminderLogRepository;


    @Override
    @Transactional
    public Reminder createReminder(Reminder reminder,UUID patientId) {
        reminder.setPatientId(patientId);
        log.info("Creating reminder for medicine: {}", reminder.getMedicine().getName());
        return reminderRepository.save(reminder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GetAllRemindersHistoryResponseProjection> getHistory(Pageable pageable,
            ReminderFilters filters,UUID patientId) {
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
            LocalDateTime takenTime,UUID patientId) {

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ReminderNotFound("Reminder not found"));

        if (!reminder.getPatientId().equals(patientId)) {
            throw new ReminderNotFound("Invalid Reminder Id");
        }
        ReminderLog reminderLog = ReminderLog.builder().status(status).reminder(reminder)
                .patientId(patientId).timestamp(takenTime).build();
        reminderLogRepository.save(reminderLog);
        // todo update the next reminder date
    }

    @Override
    public Page<GetAllRemindersResponse> getAllReminders(Pageable pageable,UUID patientId) {
        return reminderRepository.findAll(patientId, pageable);
    }

    @Override
    public Reminder getReminderById(UUID reminderId,UUID patientId) {
        return getReminder(reminderId,patientId);
    }

    private @NotNull Reminder getReminder(UUID reminderId,UUID patientId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ReminderNotFound("Reminder not found"));
        if (!reminder.getPatientId().equals(patientId)) {
            throw new ReminderNotFound("Invalid Reminder Id");
        }
        return reminder;
    }

    @Override
    @Transactional
    public Reminder updateVibration(UUID reminderId, Boolean vibrate,UUID patientId) {
        Reminder reminder = getReminder(reminderId,patientId);
        reminder.setVibrate(vibrate);
        return reminderRepository.save(reminder);
    }

    @Override
    @Transactional
    public void deleteReminder(UUID reminderId,UUID patientId) {
        Reminder reminder = getReminder(reminderId,patientId);
        reminder.getMedicine().setReminder(null);
        reminderRepository.delete(reminder);
    }

    @Override
    @Transactional
    public void disableReminder(UUID reminderId, Boolean disable,UUID patientId) {
        Reminder reminder = getReminder(reminderId,patientId);
        reminder.setDisable(disable);
    }

}
