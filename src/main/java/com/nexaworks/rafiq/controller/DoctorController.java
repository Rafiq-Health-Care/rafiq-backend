package com.nexaworks.rafiq.controller;

import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.doctor.*;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.doctor.DoctorProfileResponse;
import com.nexaworks.rafiq.dto.response.doctor.DoctorSearchResponse;
import com.nexaworks.rafiq.service.doctor.IDoctorPersistenceService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor", description = "Doctor profile and catalog")
public class DoctorController {

    private final IDoctorPersistenceService doctorService;

    @PutMapping("/price")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> setPrice(@Valid @RequestBody EditConsultationInfoRequest request) {
        doctorService.setPrice(request.price());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorProfileResponse> getDoctorById(@PathVariable UUID id) {
        DoctorProfileResponse response = doctorService.getDoctorById(id);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/search")
    public ResponseEntity<PageResponse<DoctorSearchResponse>> searchDoctor(
            @RequestBody DoctorFilter filter, Pageable pageable) {
        return ResponseEntity.ok(doctorService.search(filter, pageable));
    }

}
