package com.nexaworks.rafiq.service.patient;

import com.nexaworks.rafiq.entities.Patient;

public interface PatientService {

    void register(Patient patient);

    Patient getPatientProfile();
}
