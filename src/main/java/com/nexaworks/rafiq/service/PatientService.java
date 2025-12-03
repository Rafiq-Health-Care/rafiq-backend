package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.User;

import jakarta.validation.Valid;

public interface PatientService {
    Patient createPatientProfile(User patient);

    Patient getPatientProfile();

    Patient completePatientProfile(@Valid CreateBasicMedicalProfileRequest request);

}
