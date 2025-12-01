package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.User;

import jakarta.validation.Valid;

public interface PatientService {
    PatientProfile createPatientProfile(User patient);

    PatientProfile getPatientProfile();

    PatientProfile completePatientProfile(@Valid CreateBasicMedicalProfileRequest request);

}
