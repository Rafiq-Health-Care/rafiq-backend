package com.nexaworks.rafiq.patient.service.implementation;

import com.nexaworks.rafiq.patient.api.dto.request.CompletePatientDataRequest;
import com.nexaworks.rafiq.patient.service.WeightHistoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.patient.repository.PatientRepository;
import com.nexaworks.rafiq.patient.service.PatientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final WeightHistoryService weightHistoryService;

    @Override
    @Transactional
    public void register(Patient patient) {
        patientRepository.save(patient);
        log.info("Patient registered successfully");
    }



    @Override
    @Transactional
    public Patient completePatientProfile(CompletePatientDataRequest request, UUID patientId) {
        Patient patient = patientRepository.findById(patientId).orElseThrow(
                () -> new IllegalArgumentException("Patient not found with id: " + patientId));
        if (patient.getWeight() != request.weightInKg()) {
            weightHistoryService.logNewWeight(request.weightInKg(), patient);
        }

        fillPatientDetails(request, patient);

        return patientRepository.save(patient);
    }

    private static void fillPatientDetails(CompletePatientDataRequest request,
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
