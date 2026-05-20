package com.nexaworks.rafiq.service.medicine;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.medicine.DrugSearchResponse;
import com.nexaworks.rafiq.entities.Drug;
import com.nexaworks.rafiq.mapper.DrugMapper;
import com.nexaworks.rafiq.mapper.PageMapper;
import com.nexaworks.rafiq.repository.DrugRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DrugServiceImpl implements DrugService {
    private final DrugRepository drugRepository;
    private final PageMapper pageMapper;
    private final DrugMapper drugMapper;
    @Override
    public PageResponse<DrugSearchResponse> search(String drugName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Drug> drugs = drugRepository.searchByFullText(drugName, pageable);
        return pageMapper.mapToDrugSearchResponsePage(drugs, drugMapper);
    }

    @Override
    public Drug getDrugById(UUID drugId) {
        return drugRepository.findById(drugId)
                .orElseThrow(() -> new IllegalArgumentException("Drug not found"));
    }
}
