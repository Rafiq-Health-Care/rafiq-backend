package com.nexaworks.rafiq.controller;

import java.time.LocalDateTime;
import java.util.UUID;

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
import com.nexaworks.rafiq.service.medicine.MedicineService;
import com.nexaworks.rafiq.service.medicine.ReminderService;

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
@Tag(name = "Reminder Management", description = "Endpoints for medication reminders, history, and status tracking")
public class ReminderController {
    private final ReminderService reminderService;
    private final ReminderMapper reminderMapper;
    private final MedicineService medicineService;

    @PostMapping("/create")
    @Operation(summary = "Create reminder", description = "Creates a scheduled reminder for medication intake.")
    @ApiResponse(responseCode = "201", description = "Reminder created successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<AddReminderResponse>> createReminder(
            @RequestBody AddReminderRequest request) {
        Reminder reminder = reminderMapper.toEntity(request, medicineService);
        Reminder savedReminder = reminderService.createReminder(reminder);
        AddReminderResponse response = reminderMapper.toAddReminderResponse(savedReminder);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AddResponse<>(true, "Reminder created successfully", response));
    }
    @GetMapping("/history")
    @Operation(summary = "Get reminder history", description = "Retrieves historical record of all medication reminders with status tracking.")
    @ApiResponse(responseCode = "200", description = "Reminder history retrieved successfully", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
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
    @Operation(summary = "Mark reminder as taken", description = "Records that medication was taken at specified time.")
    @ApiResponse(responseCode = "204", description = "Reminder marked as taken")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> assignMedicineAsTaken(
            @PathVariable(name = "reminder-id") UUID reminderId,
            @RequestParam(value = "taken-time") LocalDateTime takenTime) {
        reminderService.updateReminderStatus(reminderId, ReminderStatus.TAKEN, takenTime);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/missed/{reminder-id}")
    @Operation(summary = "Mark reminder as missed", description = "Records that medication was missed for adherence tracking.")
    @ApiResponse(responseCode = "204", description = "Reminder marked as missed")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> assignMedicineAsMissed(@PathVariable("reminder-id") UUID reminderId,
            @RequestParam(value = "taken-time") LocalDateTime takenTime) {
        reminderService.updateReminderStatus(reminderId, ReminderStatus.MISSED, takenTime);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/all")
    @Operation(summary = "Get all reminders", description = "Retrieves all active reminders for medications.")
    @ApiResponse(responseCode = "200", description = "Reminders retrieved successfully", content = @Content(schema = @Schema(implementation = PageResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<PageResponse<GetAllRemindersResponse>> getAllReminders(
            @ParameterObject Pageable pageable) {
        Page<GetAllRemindersResponse> reminders = reminderService.getAllReminders(pageable);
        return ResponseEntity.ok()
                .body(new PageResponse<>(reminders.getContent(), (int) reminders.getTotalElements(),
                        reminders.getSize(), reminders.getTotalPages(), reminders.isFirst(),
                        reminders.isLast()));
    }
    @GetMapping("/{reminder-id}")
    @Operation(summary = "Get reminder by ID", description = "Retrieves detailed information about a specific reminder.")
    @ApiResponse(responseCode = "200", description = "Reminder retrieved successfully", content = @Content(schema = @Schema(implementation = GetReminderByIdResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<GetReminderByIdResponse> getReminderById(
            @PathVariable("reminder-id") UUID reminderId) {
        return ResponseEntity.ok().body(reminderMapper
                .toGetReminderByIdResponse(reminderService.getReminderById(reminderId)));
    }
    @PatchMapping("updateVibration/{vibrate}/reminder/{reminder-id}")
    @Operation(summary = "Update reminder vibration", description = "Enables or disables vibration for a specific reminder.")
    @ApiResponse(responseCode = "200", description = "Reminder vibration updated successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<AddReminderResponse>> updateVibration(
            @PathVariable("vibrate") Boolean vibrate,
            @PathVariable("reminder-id") UUID reminderId) {
        return ResponseEntity.ok()
                .body(new AddResponse<>(true, "Reminder vibration updated successfully",
                        reminderMapper.toAddReminderResponse(
                                reminderService.updateVibration(reminderId, vibrate))));

    }
    @DeleteMapping("/{reminder-id}")
    @Operation(summary = "Delete reminder", description = "Removes a reminder from the schedule.")
    @ApiResponse(responseCode = "204", description = "Reminder deleted successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteReminder(@PathVariable("reminder-id") UUID reminderId) {
        reminderService.deleteReminder(reminderId);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/disable/{reminder-id}/{disable}")
    @Operation(summary = "Disable or enable reminder", description = "Temporarily disables or re-enables a reminder without deleting it.")
    @ApiResponse(responseCode = "204", description = "Reminder status updated successfully")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> disableReminder(@PathVariable("reminder-id") UUID reminderId,
            @PathVariable("disable") Boolean disable) {
        reminderService.disableReminder(reminderId, disable);
        return ResponseEntity.noContent().build();
    }
}
