package com.nexaworks.rafiq.service.patient;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.basicMedicalProfile.CreateBasicMedicalProfileRequest;
import com.nexaworks.rafiq.dto.response.patientProfile.CompletePatientProfile;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientProfileResponse;
import com.nexaworks.rafiq.entities.Patient;
import com.nexaworks.rafiq.mapper.MedicineMapper;
import com.nexaworks.rafiq.mapper.PatientMapper;
import com.nexaworks.rafiq.mapper.TestMapper;
import com.nexaworks.rafiq.repository.PatientRepository;
import com.nexaworks.rafiq.service.authentication.AuthServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientProfileServiceImpl implements PatientProfileService {
    private final WeightHistoryService weightHistoryService;
    private final AuthServiceImpl authService;
    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final PatientMapper patientMapper;
    private final TestMapper testMapper;
    private final MedicineMapper medicineMapper;

    @Override
    @Transactional
    public PatientProfileResponse completePatientProfile(CreateBasicMedicalProfileRequest request) {
        Patient patient = (Patient) authService.getAuthenticateUser();
        if (patient.getWeight() != request.weightInKg()) {
            weightHistoryService.logNewWeight(request.weightInKg(), patient);
        }

        fillPatientDetails(request, patient);

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toResponse(savedPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public CompletePatientProfile getCompletePatientProfile() {
        Patient patient = patientService.getPatientProfile();
        return patientMapper.convertToCompleteProfile(patient, testMapper, medicineMapper);
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
