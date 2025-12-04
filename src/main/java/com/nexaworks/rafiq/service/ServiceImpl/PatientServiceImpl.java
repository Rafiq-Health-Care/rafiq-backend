package com.nexaworks.rafiq.service.ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.PatientService;
import com.nexaworks.rafiq.service.WeightHistoryService;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final WeightHistoryService weightHistoryService;
    private final AuthService authService;

    @Override
    @Transactional
    public Patient completePatientProfile(CreateBasicMedicalProfileRequest request) {
        Patient patient = (Patient) authService.getAuthenticateUser();
        if (patient.getWeight() != request.weightInKg()) {
            weightHistoryService.logNewWeight(request.weightInKg(), patient);
        }

        patient.setHeight(request.heightInCm());
        patient.setAlcoholism(request.alcoholism());
        patient.setBloodType(request.bloodType());
        patient.setCigarettesPerDay(request.cigarettesPerDay());
        patient.setDrinksPerWeek(request.drinksPerWeek());
        patient.setEmergencyContactName(request.emergencyContactName());
        patient.setEmergencyContactPhone(request.emergencyContactPhone());
        patient.setLastSmoked(request.lastSmoked());
        patient.setOccupation(request.occupation());
        patient.setPregnant(request.pregnant());
        patient.setSmokeStatus(request.smokeStatus());
        patient.setWeight(request.weightInKg());

        return patientRepository.save(patient);
    }

    @Override
    @Transactional
    public void register(Patient patient) {
        patientRepository.save(patient);
        log.info("Patient registered successfully");
    }
}
