package com.nexaworks.rafiq.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.dto.response.consultation.CallResponse;
import com.nexaworks.rafiq.idempotency.annotation.Idempotent;
import com.nexaworks.rafiq.service.consultation.ConsultationCallService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Consultation Calls", description = "Manage video/audio calls for consultations")
@RestController
@RequestMapping("/api/v1/consultations/{id}/call")
@RequiredArgsConstructor
public class ConsultationCallController {
    private final ConsultationCallService consultationCallService;

    @Idempotent(force = true)
    @PostMapping("/enter")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Enter a consultation call", responses = {
            @ApiResponse(responseCode = "200", description = "Call entered successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Consultation not found"),
            @ApiResponse(responseCode = "409", description = "Call is not active")})
    public ResponseEntity<CallResponse> enterCall(
            @Parameter(description = "UUID of the consultation", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(consultationCallService.enterCall(id));
    }

    @Idempotent(force = true)
    @PostMapping("/leave")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @Operation(summary = "Leave a consultation call", responses = {
            @ApiResponse(responseCode = "200", description = "Call left successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Consultation not found")})
    public ResponseEntity<Void> leaveCall(
            @Parameter(description = "UUID of the consultation", required = true) @PathVariable UUID id) {
        consultationCallService.leaveCall(id);
        return ResponseEntity.ok().build();
    }
}
