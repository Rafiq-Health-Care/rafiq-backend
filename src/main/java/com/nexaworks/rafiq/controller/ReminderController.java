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
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.medicine.AddResponse;
import com.nexaworks.rafiq.dto.response.reminder.AddReminderResponse;
import com.nexaworks.rafiq.dto.response.reminder.GetAllRemindersResponse;
import com.nexaworks.rafiq.dto.response.reminder.GetReminderByIdResponse;
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
    public ResponseEntity<PageResponse<GetAllRemindersHistoryResponseProjection>> getAllReminders(
            @ParameterObject Pageable pageable, @RequestBody ReminderFilters filters) {
        Page<GetAllRemindersHistoryResponseProjection> reminders = reminderService
                .getHistory(pageable, filters);
        return ResponseEntity.ok()
                .body(new PageResponse<>(reminders.getContent(), (int) reminders.getTotalElements(),
                        reminders.getSize(), reminders.getTotalPages(), reminders.isFirst(),
                        reminders.isLast()));
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
    @GetMapping("/all")
    public ResponseEntity<PageResponse<GetAllRemindersResponse>> getAllReminders(
            @ParameterObject Pageable pageable) {
        Page<GetAllRemindersResponse> reminders = reminderService.getAllReminders(pageable);
        return ResponseEntity.ok()
                .body(new PageResponse<>(reminders.getContent(), (int) reminders.getTotalElements(),
                        reminders.getSize(), reminders.getTotalPages(), reminders.isFirst(),
                        reminders.isLast()));
    }
    @GetMapping("/{reminder-id}")
    public ResponseEntity<GetReminderByIdResponse> getReminderById(
            @PathVariable("reminder-id") UUID reminderId) {
        return ResponseEntity.ok().body(reminderMapper
                .toGetReminderByIdResponse(reminderService.getReminderById(reminderId)));
    }
    @PatchMapping("updateVibration/{vibrate}/reminder/{reminder-id}")
    public ResponseEntity<AddResponse<AddReminderResponse>> updateVibration(
            @PathVariable("vibrate") Boolean vibrate,
            @PathVariable("reminder-id") UUID reminderId) {
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Reminder vibration updated successfully",
                        reminderMapper.toAddReminderResponse(
                                reminderService.updateVibration(reminderId, vibrate))));

    }
}
