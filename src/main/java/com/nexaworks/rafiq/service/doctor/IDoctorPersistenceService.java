package com.nexaworks.rafiq.service.doctor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;

import com.nexaworks.rafiq.dto.client.cloundinary.UploadResults;
import com.nexaworks.rafiq.dto.request.doctor.DoctorFilter;
import com.nexaworks.rafiq.dto.request.doctor.EducationItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.ExperienceItemRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.doctor.DoctorProfileResponse;
import com.nexaworks.rafiq.dto.response.doctor.DoctorSearchResponse;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.entities.enums.Specialization;

public interface IDoctorPersistenceService {


    void register(Doctor user, Specialization specialization, String description);

    DoctorProfileResponse setPrice(BigDecimal price);

    DoctorProfileResponse getDoctorById(UUID id);

    PageResponse<DoctorSearchResponse> search(DoctorFilter filter, Pageable pageable);
}
