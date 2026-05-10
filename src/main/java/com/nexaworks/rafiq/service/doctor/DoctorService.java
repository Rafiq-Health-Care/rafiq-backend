package com.nexaworks.rafiq.service.doctor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.dto.request.doctor.EducationItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.ExperienceItemRequest;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.enums.Specialization;

public interface DoctorService {

    void updateNationalId(UploadResults uploadResults, UUID uuid);

    void register(Doctor user, Specialization specialization, String description);

    Doctor replaceEducation(List<EducationItemRequest> education);

    Doctor replaceExperience(List<ExperienceItemRequest> experience);

    Doctor setPrice(BigDecimal price);

    Doctor getDoctorById(UUID id);

    Page<Doctor> findDoctorsBySpecialization(Specialization specialization, Pageable pageable);
}
