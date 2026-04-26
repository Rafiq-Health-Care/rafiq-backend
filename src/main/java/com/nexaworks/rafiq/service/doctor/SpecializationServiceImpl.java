package com.nexaworks.rafiq.service.doctor;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.entities.enums.Specialization;

@Service
public class SpecializationServiceImpl implements SpecializationService {

    @Override
    public List<Specialization> getSpecializations() {
        return List.of(Specialization.values());
    }
}
