package com.nexaworks.rafiq.service.patient;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.dto.response.patientProfile.CompletePatientProfile;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientProfileResponse;

public interface PatientProfileService {
    PatientProfileResponse completePatientProfile(CreateBasicMedicalProfileRequest request);

    CompletePatientProfile getCompletePatientProfile();
}
