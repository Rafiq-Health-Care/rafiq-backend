package com.nexaworks.rafiq.medication.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.shared.dto.PageResponse;
import com.nexaworks.rafiq.medication.api.dto.response.DrugSearchResponse;
import com.nexaworks.rafiq.medication.mapper.DrugMapper;
import com.nexaworks.rafiq.shared.mapper.PageMapper;
import com.nexaworks.rafiq.medication.service.DrugService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/drugs")
@RequiredArgsConstructor
public class DrugController {
    private final DrugService drugService;
    private final PageMapper pageMapper;
    private final DrugMapper drugMapper;
    @GetMapping
    public ResponseEntity<PageResponse<DrugSearchResponse>> searchDrugs(
            @RequestParam(name = "drug") String drugName,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok().body(pageMapper
                .mapToDrugSearchResponsePage(drugService.search(drugName, page, size), drugMapper));
    }
}
