package com.nexaworks.rafiq.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.dto.response.medicine.AddResponse;
import com.nexaworks.rafiq.dto.response.patientProfile.CompletePatientProfile;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientProfileResponse;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.mapper.MedicineMapper;
import com.nexaworks.rafiq.mapper.PatientMapper;
import com.nexaworks.rafiq.mapper.TestMapper;
import com.nexaworks.rafiq.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/patients/medical-profile")
@RequiredArgsConstructor
public class PatientProfileController {
    private final PatientService patientService;
    private final PatientMapper patientMapper;
    private final MedicineMapper medicineMapper;
    private final TestMapper testMapper;

    @PostMapping
    public ResponseEntity<AddResponse<PatientProfileResponse>> addPatientProfile(
            @Valid @RequestBody CreateBasicMedicalProfileRequest request) {
        PatientProfile patient = patientService.completePatientProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AddResponse<>(true,
                "Patient profile created successfully", patientMapper.toResponse(patient)));
    }
    @GetMapping
    public ResponseEntity<CompletePatientProfile> getPatientProfile() {
        PatientProfile patientProfile = patientService.getPatientProfile();
        return ResponseEntity.ok().body(
                patientMapper.convertToCompleteProfile(patientProfile, testMapper, medicineMapper));
    }

}
