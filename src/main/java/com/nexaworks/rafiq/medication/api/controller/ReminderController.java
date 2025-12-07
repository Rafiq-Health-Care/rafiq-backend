package com.nexaworks.rafiq.medication.api.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import org.quartz.SchedulerException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.medication.api.dto.request.AddReminderRequest;
import com.nexaworks.rafiq.medication.api.dto.request.GetAllRemindersHistoryResponseProjection;
import com.nexaworks.rafiq.medication.api.dto.request.ReminderFilters;
import com.nexaworks.rafiq.medication.api.dto.response.AddReminderResponse;
import com.nexaworks.rafiq.medication.api.dto.response.AddResponse;
import com.nexaworks.rafiq.medication.api.dto.response.GetAllRemindersResponse;
import com.nexaworks.rafiq.medication.api.dto.response.GetReminderByIdResponse;
import com.nexaworks.rafiq.medication.entity.enums.ReminderStatus;
import com.nexaworks.rafiq.medication.entity.model.Reminder;
import com.nexaworks.rafiq.medication.mapper.ReminderMapper;
import com.nexaworks.rafiq.medication.service.MedicineService;
import com.nexaworks.rafiq.medication.service.ReminderService;
import com.nexaworks.rafiq.shared.dto.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/reminder")
@RequiredArgsConstructor
@Tag(name = "Reminder Management")
public class ReminderController {
    private final ReminderService reminderService;
    private final ReminderMapper reminderMapper;
    private final MedicineService medicineService;

    private UUID getUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping("/create")
    @Operation(summary = "Create reminder", description = "Creates a scheduled reminder for medication intake. Automatically schedules notifications to help users never miss a dose.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<AddReminderResponse>> createReminder(
            @RequestBody AddReminderRequest request, Authentication authentication)
            throws SchedulerException {
        Reminder reminder = reminderMapper.toEntity(request);
        reminder.setMedicine(
                medicineService.getMedicineById(request.medicineId(), getUserId(authentication)));
        Reminder savedReminder = reminderService.createReminder(reminder,
                getUserId(authentication));
        AddReminderResponse response = reminderMapper.toAddReminderResponse(savedReminder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddResponse<>(true, "Reminder created successfully", response));
    }

    @GetMapping("/history")
    @Operation(summary = "Get reminder history", description = "Retrieves historical record of all medication reminders with status tracking. Enables users to review adherence patterns and medication intake history.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<GetAllRemindersHistoryResponseProjection>> getAllReminders(
            @ParameterObject Pageable pageable, @RequestBody ReminderFilters filters,
            Authentication authentication) {
        Page<GetAllRemindersHistoryResponseProjection> reminders = reminderService
                .getHistory(pageable, filters, getUserId(authentication));
        return ResponseEntity.ok()
                .body(new PageResponse<>(reminders.getContent(), (int) reminders.getTotalElements(),
                        reminders.getSize(), reminders.getTotalPages(), reminders.isFirst(),
                        reminders.isLast()));
    }

    @PostMapping("/taken/{reminder-id}")
    @Operation(summary = "Mark reminder as taken", description = "Records that medication was taken at specified time. Updates adherence tracking and prevents duplicate reminders for the same dose.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> assignMedicineAsTaken(
            @PathVariable(name = "reminder-id") UUID reminderId,
            @RequestParam(value = "taken-time") LocalDateTime takenTime,
            Authentication authentication) {
        reminderService.updateReminderStatus(reminderId, ReminderStatus.TAKEN, takenTime,
                getUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/missed/{reminder-id}")
    @Operation(summary = "Mark reminder as missed", description = "Records that medication was missed. Important for tracking adherence gaps and may trigger follow-up reminders or alerts.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> assignMedicineAsMissed(@PathVariable("reminder-id") UUID reminderId,
            @RequestParam(value = "taken-time") LocalDateTime takenTime,
            Authentication authentication) {
        reminderService.updateReminderStatus(reminderId, ReminderStatus.MISSED, takenTime,
                getUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all")
    @Operation(summary = "Get all reminders", description = "Retrieves all active reminders for user's medications. Used to display upcoming medication schedules and manage reminder settings.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<GetAllRemindersResponse>> getAllReminders(
            @ParameterObject Pageable pageable, Authentication authentication) {
        Page<GetAllRemindersResponse> reminders = reminderService.getAllReminders(pageable,
                getUserId(authentication));
        return ResponseEntity.ok()
                .body(new PageResponse<>(reminders.getContent(), (int) reminders.getTotalElements(),
                        reminders.getSize(), reminders.getTotalPages(), reminders.isFirst(),
                        reminders.isLast()));
    }

    @GetMapping("/{reminder-id}")
    @Operation(summary = "Get reminder by ID", description = "Retrieves detailed information about a specific reminder. Used for viewing reminder details and editing reminder settings.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = GetReminderByIdResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<GetReminderByIdResponse> getReminderById(
            @PathVariable("reminder-id") UUID reminderId, Authentication authentication) {
        return ResponseEntity.ok().body(reminderMapper.toGetReminderByIdResponse(
                reminderService.getReminderById(reminderId, getUserId(authentication))));
    }

    @PatchMapping("updateVibration/{vibrate}/reminder/{reminder-id}")
    @Operation(summary = "Update reminder vibration", description = "Enables or disables vibration for a specific reminder. Allows users to customize notification preferences per medication.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<AddReminderResponse>> updateVibration(
            @PathVariable("vibrate") Boolean vibrate, @PathVariable("reminder-id") UUID reminderId,
            Authentication authentication) {
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Reminder vibration updated successfully",
                        reminderMapper.toAddReminderResponse(reminderService
                                .updateVibration(reminderId, vibrate, getUserId(authentication)))));
    }

    @DeleteMapping("/{reminder-id}")
    @Operation(summary = "Delete reminder", description = "Removes a reminder from the schedule. Use when medication is discontinued or reminder is no longer needed.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteReminder(@PathVariable("reminder-id") UUID reminderId,
            Authentication authentication) {
        reminderService.deleteReminder(reminderId, getUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/disable/{reminder-id}/{disable}")
    @Operation(summary = "Disable or enable reminder", description = "Temporarily disables or re-enables a reminder without deleting it. Useful for pausing reminders during travel or temporary medication breaks.")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> disableReminder(@PathVariable("reminder-id") UUID reminderId,
            @PathVariable("disable") Boolean disable, Authentication authentication) {
        reminderService.disableReminder(reminderId, disable, getUserId(authentication));
        return ResponseEntity.noContent().build();
    }
}
