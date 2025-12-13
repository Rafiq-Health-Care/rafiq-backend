package com.nexaworks.rafiq.patient.service;

import com.nexaworks.rafiq.patient.entity.model.Patient;

import jakarta.validation.constraints.Positive;

public interface WeightHistoryService {
    void logNewWeight(@Positive Double weight, Patient patient);
}
