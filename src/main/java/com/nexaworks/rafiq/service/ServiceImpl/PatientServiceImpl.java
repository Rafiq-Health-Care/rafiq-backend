package com.nexaworks.rafiq.service.ServiceImpl;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.entities.PatientProfile;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.repository.UserRepository;
import com.nexaworks.rafiq.service.PatientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PatientProfile createPatientProfile(User patient) {
        PatientProfile patientProfile = new PatientProfile();
        patientProfile.setUser(patient);
        return patientRepository.save(patientProfile);
    }

    @Override
    public PatientProfile getPatientProfile() {
        UUID patientId = (UUID) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        User patient = userRepository.findById(patientId).orElseThrow();
        return patient.getPatientProfile();
    }

    @Override
    @Transactional
    public PatientProfile completePatientProfile(CreateBasicMedicalProfileRequest request) {
        PatientProfile patientProfile = getPatientProfile();

        patientProfile.setHeight(request.heightInCm());
        patientProfile.setAlcoholism(request.alcoholism());
        patientProfile.setBloodType(request.bloodType());
        patientProfile.setCigarettesPerDay(request.cigarettesPerDay());
        patientProfile.setDrinksPerWeek(request.drinksPerWeek());
        patientProfile.setEmergencyContactName(request.emergencyContactName());
        patientProfile.setEmergencyContactPhone(request.emergencyContactPhone());
        patientProfile.setLastSmoked(request.lastSmoked());
        patientProfile.setOccupation(request.occupation());
        patientProfile.setPregnant(request.pregnant());
        patientProfile.setSmokeStatus(request.smokeStatus());
        patientProfile.setWeight((int) Math.round(request.weightInKg()));

        return patientRepository.save(patientProfile);
    }
}
