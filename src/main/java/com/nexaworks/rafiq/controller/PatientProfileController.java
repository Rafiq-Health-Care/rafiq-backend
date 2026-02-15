package com.nexaworks.rafiq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.dto.response.medicine.AddResponse;
import com.nexaworks.rafiq.dto.response.patientProfile.CompletePatientProfile;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientProfileResponse;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.mapper.MedicineMapper;
import com.nexaworks.rafiq.mapper.PatientMapper;
import com.nexaworks.rafiq.mapper.TestMapper;
import com.nexaworks.rafiq.service.patient.PatientProfileService;
import com.nexaworks.rafiq.service.patient.PatientService;

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
@Tag(name = "Patient Profile Management", description = "Endpoints for patient medical profiles and health data")
public class PatientProfileController {

    private final PatientProfileService patientProfileService;
    private final PatientService patientService;
    private final PatientMapper patientMapper;
    private final MedicineMapper medicineMapper;
    private final TestMapper testMapper;

    @PostMapping
    @Operation(summary = "Create patient profile", description = "Creates comprehensive medical profile with health metrics and basic information.")
    @ApiResponse(responseCode = "201", description = "Patient profile created successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<PatientProfileResponse>> addPatientProfile(
            @Valid @RequestBody CreateBasicMedicalProfileRequest request) {
        Patient patient = patientProfileService.completePatientProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AddResponse<>(true,
                "Patient profile created successfully", patientMapper.toResponse(patient)));
    }
    @GetMapping
    @Operation(summary = "Get patient profile", description = "Retrieves complete patient profile including medical history and medications.")
    @ApiResponse(responseCode = "200", description = "Patient profile retrieved successfully", content = @Content(schema = @Schema(implementation = CompletePatientProfile.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<CompletePatientProfile> getPatientProfile() {
        Patient patient = patientService.getPatientProfile();
        return ResponseEntity.ok()
                .body(patientMapper.convertToCompleteProfile(patient, testMapper, medicineMapper));
    }
    @PutMapping
    @Operation(summary = "Update patient profile", description = "Updates existing medical profile information.")
    @ApiResponse(responseCode = "200", description = "Patient profile updated successfully", content = @Content(schema = @Schema(implementation = AddResponse.class)))
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<AddResponse<PatientProfileResponse>> updatePatientProfile(
            @Valid @RequestBody CreateBasicMedicalProfileRequest request) {
        Patient patient = patientProfileService.completePatientProfile(request);
        return ResponseEntity.ok().body(new AddResponse<>(true,
                "Patient profile updated successfully", patientMapper.toResponse(patient)));
    }

}
