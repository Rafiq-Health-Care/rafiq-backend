package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.Patient;

import jakarta.validation.constraints.Positive;

public interface WeightHistoryService {
    void logNewWeight(@Positive Double weight, Patient patient);
}
