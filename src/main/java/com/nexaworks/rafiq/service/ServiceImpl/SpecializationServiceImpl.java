package com.nexaworks.rafiq.service.ServiceImpl;

import com.nexaworks.rafiq.entities.Specialization;
import com.nexaworks.rafiq.repository.SpecializationRepository;
import com.nexaworks.rafiq.service.SpecializationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecializationServiceImpl implements SpecializationService {
    private final SpecializationRepository specializationRepository;
    @Override
    public Specialization getSpecialization(UUID specialization) {
        // todo handle exception
       return specializationRepository.findById(specialization).orElseThrow(()->
               new RuntimeException("Specialization not found"));
    }
}
