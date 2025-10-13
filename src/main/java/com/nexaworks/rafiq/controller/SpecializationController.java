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
    public ResponseEntity<List<SpecializationResponse>> getSpecializations(){
        return ResponseEntity.ok().body(specializationService.getSpecializations());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Specialization> getSpecializationById(@PathVariable UUID id){
        Specialization specialization = specializationService.getSpecialization(id);
        return ResponseEntity.ok().body(specialization);
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<Specialization> getSpecializationByCode(@PathVariable String code){
        Specialization specialization = specializationService.getSpecializationByCode(code);
        return ResponseEntity.ok().body(specialization);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Specialization>> getAllSpecializations(){
        List<Specialization> specializations = specializationService.getAllSpecializations();
        return ResponseEntity.ok().body(specializations);
    }
}
