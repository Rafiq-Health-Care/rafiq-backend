package com.nexaworks.rafiq.service.doctor;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.dto.response.specialization.SpecializationResponse;
import com.nexaworks.rafiq.entities.Specialization;
import com.nexaworks.rafiq.exception.custom.SpecializationNotFoundException;
import com.nexaworks.rafiq.repository.SpecializationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecializationServiceImpl implements SpecializationService {
    private final SpecializationRepository specializationRepository;

    @Override
    public Specialization getSpecialization(UUID specialization) {
        return specializationRepository.findById(specialization)
                .orElseThrow(() -> new SpecializationNotFoundException(specialization));
    }

    @Override
    public Specialization getSpecializationByCode(String code) {
        return specializationRepository.findByCode(code)
                .orElseThrow(() -> new SpecializationNotFoundException("code", code));
    }

    @Override
    public List<SpecializationResponse> getSpecializations() {
        List<Specialization> specializations = specializationRepository.findAll();
        return specializations.stream()
                .map(sp -> new SpecializationResponse(sp.getId(), sp.getName())).toList();
    }

    @Override
    public List<Specialization> getAllSpecializations() {
        return specializationRepository.findAll();
    }
}
