package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.Specialization;

import java.util.UUID;

public interface SpecializationService {
    Specialization getSpecialization(UUID specialization);
}
