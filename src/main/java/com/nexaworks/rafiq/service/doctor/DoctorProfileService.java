package com.nexaworks.rafiq.service.doctor;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.request.doctor.AddNewExperience;
import com.nexaworks.rafiq.dto.request.doctor.EditBiographyRequest;
import com.nexaworks.rafiq.dto.request.doctor.EditExperience;
import com.nexaworks.rafiq.dto.request.doctor.UpdateBasicInfoRequest;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Experience;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.entities.enums.SubSpecialization;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorProfileService implements IDoctorProfileService {
    private final DoctorRepository doctorRepository;
    private final AuthService authService;

    @Override
    @Transactional
    public void updateBiography(EditBiographyRequest request) {
        UUID userId = authService.getAuthenticateUserId();
        doctorRepository.updateBiography(userId, request.biography());
    }

    @Override
    public void updateBasicInfo(UpdateBasicInfoRequest request) {
        Doctor doctor = (Doctor) authService.getAuthenticateUser();
        validateSubSpecialization(request.specialization(), request.subSpecializations());
        doctor.setFirstName(request.firstName());
        doctor.setLastName(request.lastName());
        doctor.setSpecialization(request.specialization());
        doctor.setDescription(request.description());
        doctor.setLanguages(request.languages());
        doctor.setSubSpecializations(request.subSpecializations());
        doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public void addNewExperience(AddNewExperience request) {
        Doctor doctor = (Doctor) authService.getAuthenticateUser();
        Experience experience = Experience.builder().id(UUID.randomUUID())
                .position(request.position()).hospital(request.hospitalName())
                .startDate(request.startDate()).endDate(request.endDate())
                .description(request.description() == null ? "" : request.description())
                .current(request.currentJob()).build();
        if (doctor.getExperience() == null) {
            doctor.setExperience(new ArrayList<>());
        }
        doctor.getExperience().add(experience);
        doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public void editExperience(UUID expId, EditExperience request) {
        Doctor doctor = (Doctor) authService.getAuthenticateUser();
        doctor.getExperience().stream().filter(e -> e.getId().equals(expId)).findFirst()
                .ifPresent(e -> {
                    e.setPosition(request.position());
                    e.setHospital(request.hospitalName());
                    e.setStartDate(request.startDate());
                    e.setEndDate(request.endDate());
                    e.setDescription(request.description() == null ? "" : request.description());
                    e.setCurrent(request.currentJob());
                });
        doctorRepository.save(doctor);
    }

    private void validateSubSpecialization(@NotBlank Specialization specialization,
            @NotBlank Set<SubSpecialization> subSpecializations) {
        subSpecializations.forEach(sub -> {
            SubSpecialization.validate(sub, specialization);
        });
    }
}
