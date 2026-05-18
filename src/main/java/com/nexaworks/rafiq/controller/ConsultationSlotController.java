package com.nexaworks.rafiq.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.EditConsultationSlotRequest;
import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.EditConsultationSlotResponse;
import com.nexaworks.rafiq.dto.response.consultation.ScheduleResponse;
import com.nexaworks.rafiq.entities.ConsultationSlot;
import com.nexaworks.rafiq.mapper.ConsultationSlotMapper;
import com.nexaworks.rafiq.service.consultation.IConsultationSearchService;
import com.nexaworks.rafiq.service.consultation.IConsultationSlotService;

import io.swagger.v3.oas.annotations.Operation;
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
    private final ConsultationSlotMapper mapper;
    private final IConsultationSearchService searchService;

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
        ConsultationSlot slot = IConsultationSlotService.editConsultation(request, id);
        return ResponseEntity.ok(mapper.toEditResponse(slot));
    }

    @PostMapping("/schedule/search")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get doctor schedule", responses = {
            @ApiResponse(responseCode = "200", description = "Schedule retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid filter"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<PageResponse<ScheduleResponse>> getSchedule(
            @Valid @RequestBody ScheduleFilter filter, @ParameterObject Pageable pageable) {
        Page<ConsultationSlot> slotPage = searchService.getDoctorSchedule(filter, pageable);
        return ResponseEntity.ok().body(mapper.toSchedulePageResponse(slotPage));

    }
}
