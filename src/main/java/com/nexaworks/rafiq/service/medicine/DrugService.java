package com.nexaworks.rafiq.service.medicine;

import java.util.UUID;

import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.medicine.DrugSearchResponse;
import com.nexaworks.rafiq.entities.Drug;

public interface DrugService {
    PageResponse<DrugSearchResponse> search(String drugName, int page, int size);

    Drug getDrugById(UUID drugId);
}
