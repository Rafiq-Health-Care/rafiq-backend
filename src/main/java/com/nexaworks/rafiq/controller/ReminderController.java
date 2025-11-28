package com.nexaworks.rafiq.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.reminder.AddReminderRequest;
import com.nexaworks.rafiq.dto.request.reminder.GetAllRemindersHistoryResponseProjection;
import com.nexaworks.rafiq.dto.request.reminder.ReminderFilters;
import com.nexaworks.rafiq.dto.response.medicine.AddResponse;
import com.nexaworks.rafiq.dto.response.reminder.AddReminderResponse;
import com.nexaworks.rafiq.entities.Reminder;
import com.nexaworks.rafiq.entities.enums.ReminderStatus;
import com.nexaworks.rafiq.mapper.ReminderMapper;
import com.nexaworks.rafiq.service.MedicineService;
import com.nexaworks.rafiq.service.ReminderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reminder")
@RequiredArgsConstructor
public class ReminderController {
    private final ReminderService reminderService;
    private final ReminderMapper reminderMapper;
    private final MedicineService medicineService;

    @PostMapping("/create")
    public ResponseEntity<AddResponse<AddReminderResponse>> createReminder(
            @RequestBody AddReminderRequest request) throws SchedulerException {
        Reminder reminder = reminderMapper.toEntity(request, medicineService);
        Reminder savedReminder = reminderService.createReminder(reminder);
        AddReminderResponse response = reminderMapper.toAddReminderResponse(savedReminder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddResponse<>(true, "Reminder created successfully", response));
    }
    @GetMapping("/history")
    public ResponseEntity<Page<GetAllRemindersHistoryResponseProjection>> getAllReminders(
            @ParameterObject Pageable pageable, @RequestBody ReminderFilters filters) {
        return ResponseEntity.ok().body(reminderService.getHistory(pageable, filters));
    }
    @PostMapping("/taken/{reminder-id}")
    public ResponseEntity<Void> assignMedicineAsTaken(
            @PathVariable(name = "reminder-id") UUID reminderId,
            @RequestParam(value = "taken-time") LocalDateTime takenTime) {
        reminderService.updateReminderStatus(reminderId, ReminderStatus.TAKEN, takenTime);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/missed/{reminder-id}")
    public ResponseEntity<Void> assignMedicineAsMissed(@PathVariable("reminder-id") UUID reminderId,
            @RequestParam(value = "taken-time") LocalDateTime takenTime) {
        reminderService.updateReminderStatus(reminderId, ReminderStatus.MISSED, takenTime);
        return ResponseEntity.noContent().build();
    }

}
