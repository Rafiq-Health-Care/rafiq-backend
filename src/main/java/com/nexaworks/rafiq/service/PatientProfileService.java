package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.entities.Patient;

public interface PatientProfileService {
    Patient completePatientProfile(CreateBasicMedicalProfileRequest request);
}
