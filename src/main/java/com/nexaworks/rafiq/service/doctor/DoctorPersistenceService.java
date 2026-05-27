package com.nexaworks.rafiq.service.doctor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.dto.request.doctor.DoctorFilter;
import com.nexaworks.rafiq.dto.request.doctor.EducationItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.ExperienceItemRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.doctor.DoctorSearchResponse;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.DoctorSearchView;
import com.nexaworks.rafiq.entities.Education;
import com.nexaworks.rafiq.entities.Experience;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.exception.custom.user.UserException;
import com.nexaworks.rafiq.exception.custom.user.UserNotFoundException;
import com.nexaworks.rafiq.mapper.DoctorMapper;
import com.nexaworks.rafiq.repository.DoctorRepository;
import com.nexaworks.rafiq.repository.DoctorSearchViewRepository;
import com.nexaworks.rafiq.repository.specification.DoctorSpecification;
import com.nexaworks.rafiq.service.authentication.AuthService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorPersistenceService implements IDoctorPersistenceService {
    private final DoctorRepository doctorRepository;
    private final AuthService authService;
    private final DoctorMapper doctorMapper;
    private final DoctorSearchViewRepository doctorSearchViewRepository;



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
    public com.nexaworks.rafiq.dto.response.doctor.DoctorProfileResponse setPrice(
            BigDecimal price) {
        Doctor doctor = requireAuthenticatedDoctor();
        doctor.setPrice(price);
        Doctor saved = doctorRepository.save(doctor);
        return doctorMapper.toProfileResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public com.nexaworks.rafiq.dto.response.doctor.DoctorProfileResponse getDoctorById(UUID id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with id: " + id));
        return doctorMapper.toProfileResponse(doctor);
    }

    @Override
    public PageResponse<DoctorSearchResponse> search(DoctorFilter filter, Pageable pageable) {
        Page<DoctorSearchView> results = doctorSearchViewRepository
                .findAll(DoctorSpecification.search(filter), pageable);

        return PageResponse.of(results,
                v -> new DoctorSearchResponse(v.getPersonalPhoto(), v.getFirstName(),
                        v.getLastName(), v.getSpecialization(), v.getNextAvailable(), v.getPrice(),
                        v.getRating(), v.getExperienceYears(), v.getDoctorId()));
    }

    private Doctor requireAuthenticatedDoctor() {
        UUID id = authService.getAuthenticateUserId();
        return doctorRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Doctor not found with id: " + id));
    }
}
