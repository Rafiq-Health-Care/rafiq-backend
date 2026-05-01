package com.nexaworks.rafiq.controller;

import com.nexaworks.rafiq.dto.request.consultation.AddConsultationRequest;
import com.nexaworks.rafiq.dto.request.consultation.ScheduleFilter;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.consultation.ConsultationResponse;
import com.nexaworks.rafiq.entities.Consultation;
import com.nexaworks.rafiq.entities.enums.PaymentProvider;
import com.nexaworks.rafiq.mapper.ConsultationMapper;
import com.nexaworks.rafiq.service.consultation.ConsultationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/consultation")
@RequiredArgsConstructor
@Tag(name = "Consultation", description = "Endpoints for consultation")
public class ConsultationController {
    private final ConsultationService consultationService;
    private final ConsultationMapper mapper;



    @PostMapping("/add")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ConsultationResponse> addConsultation(@Valid @RequestBody AddConsultationRequest request){
        Consultation consultation = mapper.toEntity(request);
        consultation = consultationService.add(consultation);
        return ResponseEntity.ok(mapper.toDto(consultation));

    }
    @GetMapping("/schedule")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PageResponse<ConsultationResponse>> getSchedule(@Valid @RequestBody ScheduleFilter filter,Pageable pageable){
        Page<Consultation> consultations = consultationService.getDoctorSchedule(filter,pageable);
        return ResponseEntity.ok(mapper.toPageResponse(consultations));
    }
    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ConsultationResponse> editConsultation(@Valid @RequestBody AddConsultationRequest request
    ,@PathVariable UUID id) {
        Consultation consultation = consultationService.editConsultation(request,id);
        return ResponseEntity.ok(mapper.toDto(consultation));
    }
    @PatchMapping("/cancel/{id}")
    public ResponseEntity<?> cancelConsultation(@PathVariable UUID id,@RequestParam String reason){
        consultationService.cancel(id,reason);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reserve/{id}")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String > reserveConsultation(@PathVariable UUID id, @RequestParam PaymentProvider provider){
       String paymentKey = consultationService.reserve(id,provider);
       return ResponseEntity.ok(paymentKey);
    }


}
