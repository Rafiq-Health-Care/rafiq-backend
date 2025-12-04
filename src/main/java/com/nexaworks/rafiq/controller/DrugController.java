package com.nexaworks.rafiq.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.medicine.DrugSearchResponse;
import com.nexaworks.rafiq.mapper.DrugMapper;
import com.nexaworks.rafiq.mapper.PageMapper;
import com.nexaworks.rafiq.service.DrugService;

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
