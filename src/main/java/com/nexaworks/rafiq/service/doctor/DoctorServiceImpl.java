package com.nexaworks.rafiq.service.doctor;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.Specialization;
import com.nexaworks.rafiq.exception.custom.UserNotFoundException;
import com.nexaworks.rafiq.repository.DoctorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {
    private final DoctorRepository doctorRepository;
    private final SpecializationService specializationService;

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
    public void register(Doctor doctor, UUID specialization, String description) {
        Specialization doctorSpecialization = specializationService
                .getSpecialization(specialization);
        doctor.setSpecialization(doctorSpecialization);
        doctor.setDescription(description);
        doctorRepository.save(doctor);
        log.info("Doctor registered successfully");
    }
}
