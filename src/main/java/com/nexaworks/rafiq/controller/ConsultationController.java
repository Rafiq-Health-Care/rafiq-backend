package com.nexaworks.rafiq.controller;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.consultation.CancelConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.ReserveConsultationRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationResponse;
import com.nexaworks.rafiq.dto.response.consultation.PatientConsultationResponse;
import com.nexaworks.rafiq.dto.response.consultation.ReserveConsultationResponse;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;
import com.nexaworks.rafiq.service.consultation.IConsultationCancellationService;
import com.nexaworks.rafiq.service.consultation.IConsultationSearchService;
import com.nexaworks.rafiq.service.consultation.IReservationService;

import dev.once.annotation.Idempotent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/consultation")
@RequiredArgsConstructor
public class ConsultationController {
    private final IReservationService reservationService;
    private final IConsultationCancellationService cancellationService;
    private final IConsultationSearchService searchService;

    @Idempotent(force = true)
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Reserve a consultation slot", responses = {
            @ApiResponse(responseCode = "201", description = "Reservation created, payment key returned"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Slot not found"),
            @ApiResponse(responseCode = "409", description = "Slot not available")})
    public ResponseEntity<ReserveConsultationResponse> reserveConsultation(
            @Valid @RequestBody ReserveConsultationRequest request) {
        String paymentKey = reservationService.reserve(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ReserveConsultationResponse(paymentKey));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Cancel a consultation", responses = {
            @ApiResponse(responseCode = "200", description = "Consultation cancelled"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Consultation not found"),
            @ApiResponse(responseCode = "409", description = "Consultation cannot be cancelled")})
    public ResponseEntity<Void> cancelConsultation(@PathVariable UUID id,
            @Valid @RequestBody CancelConsultationRequest request) {
        cancellationService.cancel(id, request.reason());
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT')")
    @Operation(summary = "Get consultation by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Consultation retrieved"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Consultation not found")})
    public ResponseEntity<ConsultationResponse> getConsultation(
            @Parameter(description = "UUID of the consultation", required = true) @PathVariable UUID id) {
        ConsultationResponse response = searchService.getConsultation(id);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/patient/{status}")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient consultations by status", responses = {
            @ApiResponse(responseCode = "200", description = "Consultations retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid status"),
            @ApiResponse(responseCode = "403", description = "Access denied")})
    public ResponseEntity<PageResponse<PatientConsultationResponse>> getPatientConsultationsByStatus(
            @Parameter(description = "Consultation status", example = "UPCOMING", required = true) @PathVariable ConsultationStatus status,
            @ParameterObject Pageable pageable) {
        PageResponse<PatientConsultationResponse> response = searchService
                .getPatientConsultationsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }
}
