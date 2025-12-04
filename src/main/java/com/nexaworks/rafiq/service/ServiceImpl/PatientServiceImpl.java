package com.nexaworks.rafiq.service.ServiceImpl;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.PatientService;
import com.nexaworks.rafiq.service.WeightHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final WeightHistoryService weightHistoryService;

    @Override
    public Patient getPatientProfile() {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return patientRepository.findById(userId).orElseThrow();
    }

    @Override
    @Transactional
    public Patient completePatientProfile(CreateBasicMedicalProfileRequest request) {
        Patient patient = getPatientProfile();
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
}
