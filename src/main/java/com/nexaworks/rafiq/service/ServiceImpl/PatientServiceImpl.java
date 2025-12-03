package com.nexaworks.rafiq.service.ServiceImpl;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.entities.User;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.PatientService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientServiceImpl implements PatientService {
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public Patient createPatientProfile(User user) {
        Patient patient = new Patient();
        // Copy User properties to Patient (since Patient extends User)
        patient.setId(user.getId());
        patient.setEmail(user.getEmail());
        patient.setPassword(user.getPassword());
        patient.setFirstName(user.getFirstName());
        patient.setLastName(user.getLastName());
        patient.setPhone(user.getPhone());
        patient.setBirthDate(user.getBirthDate());
        patient.setActive(user.isActive());
        patient.setLocked(user.isLocked());
        patient.setEnabled(user.isEnabled());
        patient.setNotificationToken(user.getNotificationToken());
        patient.setGender(user.getGender());
        patient.setRoles(user.getRoles());
        // Audit fields are inherited from BaseEntity and handled automatically

        return patientRepository.save(patient);
    }

    @Override
    public Patient getPatientProfile() {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return patientRepository.findById(userId).orElseThrow();
    }

    @Override
    @Transactional
    public Patient completePatientProfile(CreateBasicMedicalProfileRequest request) {
        Patient patient = getPatientProfile();

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
        patient.setWeight((int) Math.round(request.weightInKg()));

        return patientRepository.save(patient);
    }
}
