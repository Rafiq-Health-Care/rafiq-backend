package com.nexaworks.rafiq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.dto.response.medicine.AddResponse;
import com.nexaworks.rafiq.dto.response.patientProfile.CompletePatientProfile;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientProfileResponse;
import com.nexaworks.rafiq.service.patient.PatientProfileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/patients/medical-profile")
@RequiredArgsConstructor
@Tag(name = "Patient Profile Management", description = "Endpoints for patient medical profiles and health data")
// under development
public class PatientProfileController {

    private final PatientProfileService patientProfileService;

    @PostMapping
    @Operation(summary = "Create patient profile", description = "Creates comprehensive medical profile with health metrics and basic information.")
    @ApiResponse(responseCode = "201", description = "Patient profile created successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<PatientProfileResponse>> addPatientProfile(
            @Valid @RequestBody CreateBasicMedicalProfileRequest request) {
        PatientProfileResponse patientProfile = patientProfileService
                .completePatientProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new AddResponse<>(true, "Patient profile created successfully", patientProfile));
    }
    @GetMapping
    @Operation(summary = "Get patient profile", description = "Retrieves complete patient profile including medical history and medications.")
    @ApiResponse(responseCode = "200", description = "Patient profile retrieved successfully", content = @Content(schema = @Schema(implementation = CompletePatientProfile.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CompletePatientProfile> getPatientProfile() {
        return ResponseEntity.ok().body(patientProfileService.getCompletePatientProfile());
    }
    @PutMapping
    @Operation(summary = "Update patient profile", description = "Updates existing medical profile information.")
    @ApiResponse(responseCode = "200", description = "Patient profile updated successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<PatientProfileResponse>> updatePatientProfile(
            @Valid @RequestBody CreateBasicMedicalProfileRequest request) {
        PatientProfileResponse patientProfile = patientProfileService
                .completePatientProfile(request);
        return ResponseEntity.ok().body(
                new AddResponse<>(true, "Patient profile updated successfully", patientProfile));
    }

}
