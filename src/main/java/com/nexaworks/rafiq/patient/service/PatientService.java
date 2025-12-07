package com.nexaworks.rafiq.patient.service;

import java.util.UUID;

import com.nexaworks.rafiq.patient.api.dto.request.CompletePatientDataRequest;
import com.nexaworks.rafiq.patient.entity.model.Patient;

public interface PatientService {

    void register(String email, String firstName, String lastName, UUID userId);

    Patient completePatientProfile(CompletePatientDataRequest request, UUID patientId);

}
