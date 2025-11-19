package com.nexaworks.rafiq.service;

import org.springframework.data.domain.Page;

import com.nexaworks.rafiq.entities.Drug;

public interface DrugService {
    Page<Drug> search(String drugName, int page, int size);
}
