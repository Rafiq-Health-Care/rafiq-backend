package com.nexaworks.rafiq.patient.service.implementation;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.patient.api.dto.request.CompletePatientDataRequest;
import com.nexaworks.rafiq.patient.entity.model.Patient;
import com.nexaworks.rafiq.patient.repository.PatientRepository;
import com.nexaworks.rafiq.patient.service.PatientService;
import com.nexaworks.rafiq.patient.service.WeightHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final WeightHistoryService weightHistoryService;

    @Override
    @Transactional
    public void register(String email, String firstName, String lastName, UUID userId) {
        Patient patient = Patient.builder().email(email).firstName(firstName).lastName(lastName)
                .id(userId).build();
        patientRepository.save(patient);
    }

    @Override
    @Transactional
    public Patient completePatientProfile(CompletePatientDataRequest request, UUID patientId) {
        // todo handle the failure of the registration patient
        Patient patient = patientRepository.findById(patientId).orElseThrow(
                () -> new IllegalArgumentException("Patient not found with id: " + patientId));
        if (patient.getWeight() != request.weightInKg()) {
            weightHistoryService.logNewWeight(request.weightInKg(), patient);
        }

        fillPatientDetails(request, patient);

        return patientRepository.save(patient);
    }

    private static void fillPatientDetails(CompletePatientDataRequest request, Patient patient) {
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
