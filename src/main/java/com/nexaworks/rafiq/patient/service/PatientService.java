package com.nexaworks.rafiq.patient.service;

import java.util.UUID;

import com.nexaworks.rafiq.patient.api.dto.request.CompletePatientDataRequest;
import com.nexaworks.rafiq.patient.entity.model.Patient;

public interface PatientService {

    void register(Patient patient);

    Patient completePatientProfile(CompletePatientDataRequest request, UUID patientId);

}
