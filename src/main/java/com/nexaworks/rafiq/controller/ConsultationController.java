package com.nexaworks.rafiq.controller;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationCreatedResponse;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.mapper.ConsultationMapper;
import com.nexaworks.rafiq.service.consultation.ConsultationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consultation")
@RequiredArgsConstructor
@Tag(name = "Consultation", description = "Endpoints for consultation")
public class ConsultationController {
    private final ConsultationService consultationService;
    private final ConsultationMapper mapper;

    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConsultationCreatedResponse> addConsultation(@RequestBody AddConsultationRequest request){
        Consultation consultation = consultationService.add(request);
        return ResponseEntity.ok(mapper.toDto(consultation));

    }

}
