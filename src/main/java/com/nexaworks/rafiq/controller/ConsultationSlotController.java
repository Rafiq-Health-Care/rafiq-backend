package com.nexaworks.rafiq.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.EditConsultationSlotRequest;
import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationSlotResponse;
import com.nexaworks.rafiq.dto.response.consultation.DoctorConsultationResponse;
import com.nexaworks.rafiq.dto.response.consultation.EditConsultationSlotResponse;
import com.nexaworks.rafiq.dto.response.consultation.ScheduleResponse;
import com.nexaworks.rafiq.service.consultation.IConsultationSearchService;
import com.nexaworks.rafiq.service.consultation.IConsultationSlotHoldingService;
import com.nexaworks.rafiq.service.consultation.IConsultationSlotService;

import dev.once.annotation.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/slot")
@RequiredArgsConstructor
@Tag(name = "Consultation Slots", description = "Manage and search doctor consultation slots")
public class ConsultationSlotController {
    private final IConsultationSlotService IConsultationSlotService;
    private final IConsultationSearchService searchService;
    private final IConsultationSlotHoldingService holdingService;

    @Idempotent(force = true)
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Add consultation slot", responses = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<?> addConsultation(@Valid @RequestBody AddConsultationRequest request) {
        IConsultationSlotService.add(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Edit consultation slot", responses = {
            @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = EditConsultationSlotResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Not found")})
    public ResponseEntity<EditConsultationSlotResponse> editConsultation(
            @Valid @RequestBody EditConsultationSlotRequest request, @PathVariable UUID id) {
        EditConsultationSlotResponse response = IConsultationSlotService.editConsultation(request,
                id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/schedule/search")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get doctor schedule", responses = {
            @ApiResponse(responseCode = "200", description = "Schedule retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid filter"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<PageResponse<ScheduleResponse>> getSchedule(
            @Valid @RequestBody ScheduleFilter filter, @ParameterObject Pageable pageable) {
        PageResponse<ScheduleResponse> response = searchService.getDoctorSchedule(filter, pageable);
        return ResponseEntity.ok().body(response);

    }
    @PutMapping("/{id}/hold")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Hold a consultation slot", responses = {
            @ApiResponse(responseCode = "200", description = "Slot held successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "409", description = "Slot already held")})
    public ResponseEntity<Void> holdConsultation(@PathVariable UUID id) {
        holdingService.hold(id);
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{id}/release")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Release a held consultation slot", responses = {
            @ApiResponse(responseCode = "200", description = "Slot released successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<Void> releaseConsultation(@PathVariable UUID id) {
        holdingService.release(id);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/doctor/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Get available consultation slots for a doctor", responses = {
            @ApiResponse(responseCode = "200", description = "Slots retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Doctor not found")})
    public ResponseEntity<PageResponse<DoctorConsultationResponse>> getDoctorConsultations(
            @Parameter(description = "UUID of the doctor", required = true) @PathVariable UUID id,
            @ParameterObject Pageable pageable) {
        PageResponse<DoctorConsultationResponse> response = searchService
                .getDoctorAvailableSlots(id, pageable);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/doctor/upcoming")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get upcoming consultation slots for the logged-in doctor", responses = {
            @ApiResponse(responseCode = "200", description = "Upcoming slots retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<PageResponse<ConsultationSlotResponse>> getDoctorUpcoming(
            @ParameterObject Pageable pageable) {
        PageResponse<ConsultationSlotResponse> response = searchService.getDoctorUpcoming(pageable);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get consultation slot by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Slot retrieved", content = @Content(schema = @Schema(implementation = ConsultationSlotResponse.class))),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Slot not found")})
    public ResponseEntity<ConsultationSlotResponse> getConsultationSlot(
            @Parameter(description = "UUID of the consultation slot", required = true) @PathVariable UUID id) {
        ConsultationSlotResponse response = searchService.getConsultationSlot(id);
        return ResponseEntity.ok(response);
    }

}
