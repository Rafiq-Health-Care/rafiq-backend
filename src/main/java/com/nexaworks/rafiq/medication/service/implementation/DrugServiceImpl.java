package com.nexaworks.rafiq.medication.service.implementation;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.medication.entity.model.Drug;
import com.nexaworks.rafiq.medication.repository.DrugRepository;
import com.nexaworks.rafiq.medication.service.DrugService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DrugServiceImpl implements DrugService {
    private final DrugRepository drugRepository;
    @Override
    public Page<Drug> search(String drugName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return drugRepository.searchByFullText(drugName, pageable);
    }

    @Override
    public Drug getDrugById(UUID drugId) {
        return drugRepository.findById(drugId)
                .orElseThrow(() -> new IllegalArgumentException("Drug not found"));
    }
}
