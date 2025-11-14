package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.User;

public interface PatientService {
  PatientProfile createPatientProfile(User patient);
}
