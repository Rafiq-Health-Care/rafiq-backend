package com.nexaworks.rafiq.doctor.service;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.doctor.api.dto.SpecializationResponse;
import com.nexaworks.rafiq.doctor.entity.model.Specialization;

public interface SpecializationService {
    Specialization getSpecialization(UUID specialization);

    Specialization getSpecializationByCode(String code);

    List<SpecializationResponse> getSpecializations();

    List<Specialization> getAllSpecializations();
}
