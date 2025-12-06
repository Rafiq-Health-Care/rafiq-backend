package com.nexaworks.rafiq.doctor.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.doctor.api.dto.SpecializationResponse;
import com.nexaworks.rafiq.doctor.service.SpecializationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/specialization")
@RequiredArgsConstructor
public class SpecializationController {
    private final SpecializationService specializationService;

    @GetMapping
    public ResponseEntity<List<SpecializationResponse>> getSpecializations() {
        return ResponseEntity.ok().body(specializationService.getSpecializations());
    }
}
