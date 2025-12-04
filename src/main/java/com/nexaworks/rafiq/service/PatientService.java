package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.entities.Patient;

import jakarta.validation.Valid;

public interface PatientService {

    Patient getPatientProfile();

    Patient completePatientProfile(@Valid CreateBasicMedicalProfileRequest request);

}
