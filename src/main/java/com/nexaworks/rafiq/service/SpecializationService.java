package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.dto.response.SpecializationResponse;
import com.nexaworks.rafiq.entities.Specialization;

import java.util.List;
import java.util.UUID;

public interface SpecializationService {
    Specialization getSpecialization(UUID specialization);
    
    Specialization getSpecializationByCode(String code);

    List<SpecializationResponse> getSpecializations();
    
    List<Specialization> getAllSpecializations();
}
