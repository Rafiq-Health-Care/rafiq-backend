package com.nexaworks.rafiq.dto.response.patientProfile;

import java.util.List;

import com.nexaworks.rafiq.dto.response.labTest.TestResponse;
import com.nexaworks.rafiq.dto.response.medicine.MedicinePreview;

public record CompletePatientProfile(PatientProfileResponse patientProfile,
        List<TestResponse> tests, List<MedicinePreview> medicines) {
}
