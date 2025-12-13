package com.nexaworks.rafiq.medication.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.nexaworks.rafiq.medication.entity.model.Drug;

public interface DrugService {
    Page<Drug> search(String drugName, int page, int size);

    Drug getDrugById(UUID drugId);
}
