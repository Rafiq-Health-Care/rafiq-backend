package com.nexaworks.rafiq.patient.api.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.medication.api.dto.response.AddResponse;
import com.nexaworks.rafiq.patient.api.dto.request.CompletePatientDataRequest;
import com.nexaworks.rafiq.patient.api.dto.response.CompletePatientDataResponse;
import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.patient.mapper.PatientMapper;
import com.nexaworks.rafiq.patient.service.PatientService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/patients/medical-profile")
@RequiredArgsConstructor
@Tag(name = "Patient Profile Management")
public class PatientController {

    private final PatientService patientService;
    private final PatientMapper patientMapper;

    private UUID getUserId(Authentication authentication) {
        return (UUID) authentication.getPrincipal();
    }

    @PostMapping
    @Operation(summary = "Complete patient medical profile", description = "Creates comprehensive medical profile with health metrics, lifestyle information, and emergency contacts. Enables personalized healthcare recommendations and better treatment planning.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<CompletePatientDataResponse>> addPatientProfile(
            @Valid @RequestBody CompletePatientDataRequest request, Authentication authentication) {
        Patient patient = patientService.completePatientProfile(request, getUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(new AddResponse<>(true,
                "Patient profile created successfully", patientMapper.toResponse(patient)));
    }

    @PutMapping
    @Operation(summary = "Update patient medical profile", description = "Updates existing medical profile information. Automatically tracks weight changes in history. Use when health metrics or personal information changes.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<CompletePatientDataResponse>> updatePatientProfile(
            @Valid @RequestBody CompletePatientDataRequest request, Authentication authentication) {
        Patient patient = patientService.completePatientProfile(request, getUserId(authentication));
        return ResponseEntity.ok().body(new AddResponse<>(true,
                "Patient profile updated successfully", patientMapper.toResponse(patient)));
    }
}
