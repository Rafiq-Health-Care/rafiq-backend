package com.nexaworks.rafiq.service.doctor;

import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.Specialization;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.dto.response.specialization.SpecializationResponse;

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
    public List<Specialization> getSpecializations() {
        return List.of(Specialization.values());
    }

}
