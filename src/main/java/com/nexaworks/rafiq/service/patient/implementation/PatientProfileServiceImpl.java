package com.nexaworks.rafiq.service.patient.implementation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.authentication.implementaion.AuthServiceImpl;
import com.nexaworks.rafiq.service.patient.PatientProfileService;
import com.nexaworks.rafiq.service.patient.WeightHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientProfileServiceImpl implements PatientProfileService {
    private final WeightHistoryService weightHistoryService;
    private final AuthServiceImpl authService;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public Patient completePatientProfile(CreateBasicMedicalProfileRequest request) {
        Patient patient = (Patient) authService.getAuthenticateUser();
        if (patient.getWeight() != request.weightInKg()) {
            weightHistoryService.logNewWeight(request.weightInKg(), patient);
        }

        fillPatientDetails(request, patient);

        return patientRepository.save(patient);
    }

    private static void fillPatientDetails(CreateBasicMedicalProfileRequest request,
            Patient patient) {
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
    }

}
