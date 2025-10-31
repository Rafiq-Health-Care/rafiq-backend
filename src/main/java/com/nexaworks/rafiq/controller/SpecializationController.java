package com.nexaworks.rafiq.controller;

import com.nexaworks.rafiq.dto.response.SpecializationResponse;
import com.nexaworks.rafiq.entities.Specialization;
import com.nexaworks.rafiq.service.SpecializationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
