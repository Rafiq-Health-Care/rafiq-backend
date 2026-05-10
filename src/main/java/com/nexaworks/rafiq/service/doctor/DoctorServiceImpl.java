package com.nexaworks.rafiq.service.doctor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.dto.request.doctor.EducationItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.ExperienceItemRequest;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Education;
import com.nexaworks.rafiq.entities.Experience;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.exception.custom.UserException;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.mapper.DoctorMapper;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;
    private final AuthService authService;
    private final DoctorMapper doctorMapper;

    @Override
    @Transactional
    public void updateNationalId(UploadResults uploadResults, UUID doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(
                () -> new UserNotFoundException("Doctor not found with id: " + doctorId));
        doctor.setNationalId(uploadResults.url());
        doctor.setPublicId(uploadResults.publicId());
    }

    @Override
    @Transactional
    public void register(Doctor doctor, Specialization specialization, String description) {
        doctor.setSpecialization(specialization);
        doctor.setDescription(description);
        doctorRepository.save(doctor);
        log.info("Doctor registered successfully");
    }

    @Override
    @Transactional
    public Doctor replaceEducation(List<EducationItemRequest> education) {
        Doctor doctor = requireAuthenticatedDoctor();
        if (education == null) {
            throw new UserException("Education payload is required");
        }
        List<Education> existing = doctor.getEducation();
        if (existing != null && !existing.isEmpty()) {
            log.warn(
                    "Replacing entire education list for doctor {} ({} existing entries discarded)",
                    doctor.getId(), existing.size());
        }
        List<Education> persisted = education.stream().map(doctorMapper::toEducationEntity)
                .toList();
        doctor.setEducation(new ArrayList<>(persisted));
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public Doctor replaceExperience(List<ExperienceItemRequest> experience) {
        Doctor doctor = requireAuthenticatedDoctor();
        if (experience == null) {
            throw new UserException("Experience payload is required");
        }
        List<Experience> existing = doctor.getExperience();
        if (existing != null && !existing.isEmpty()) {
            log.warn(
                    "Replacing entire experience list for doctor {} ({} existing entries discarded)",
                    doctor.getId(), existing.size());
        }
        List<Experience> persisted = experience.stream().map(doctorMapper::toExperienceEntity)
                .toList();
        doctor.setExperience(new ArrayList<>(persisted));
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional
    public Doctor setPrice(BigDecimal price) {
        Doctor doctor = requireAuthenticatedDoctor();
        doctor.setPrice(price);
        return doctorRepository.save(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public Doctor getDoctorById(UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with id: " + id));
    }

    private Doctor requireAuthenticatedDoctor() {
        UUID id = authService.getAuthenticateUserId();
        return doctorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with id: " + id));
    }
}
