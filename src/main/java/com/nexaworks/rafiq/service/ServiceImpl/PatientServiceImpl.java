package com.nexaworks.rafiq.service.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.PatientService;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final AuthService authService;

    @Override
    @Transactional
    public void register(Patient patient) {
        patientRepository.save(patient);
        log.info("Patient registered successfully");
    }

    @Override
    public Patient getPatientProfile() {
        return (Patient) authService.getAuthenticateUser();
    }
}
