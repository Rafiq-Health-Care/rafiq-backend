package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    @Override
    public PatientProfile createPatientProfile(User patient) {
        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setUser(patient);
        return patientRepository.save(patientProfile);
    }
}
