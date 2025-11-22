package com.nexaworks.rafiq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.dto.request.reminder.AddReminderRequest;
import com.nexaworks.rafiq.dto.response.medicine.AddResponse;
import com.nexaworks.rafiq.dto.response.reminder.AddReminderResponse;
import com.nexaworks.rafiq.entities.Reminder;
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
            @RequestBody AddReminderRequest request) {
        Reminder reminder = reminderMapper.toEntity(request, medicineService);
        Reminder savedReminder = reminderService.createReminder(reminder);
        AddReminderResponse response = reminderMapper.toAddReminderResponse(savedReminder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddResponse<>(true, "Reminder created successfully", response));
    }
}
