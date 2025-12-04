package com.nexaworks.rafiq.service.doctor;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.dto.response.specialization.SpecializationResponse;
import com.nexaworks.rafiq.entities.Specialization;

public interface SpecializationService {
    Specialization getSpecialization(UUID specialization);

    Specialization getSpecializationByCode(String code);

    List<SpecializationResponse> getSpecializations();

    List<Specialization> getAllSpecializations();
}
