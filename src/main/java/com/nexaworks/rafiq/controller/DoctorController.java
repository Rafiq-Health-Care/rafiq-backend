package com.nexaworks.rafiq.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.doctor.DoctorFilter;
import com.nexaworks.rafiq.dto.request.doctor.EducationItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.ExperienceItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.SetDoctorPriceRequest;
import com.nexaworks.rafiq.dto.response.common.PageResponse;
import com.nexaworks.rafiq.dto.response.doctor.DoctorProfileResponse;
import com.nexaworks.rafiq.dto.response.doctor.DoctorSearchResponse;
import com.nexaworks.rafiq.service.doctor.DoctorService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor", description = "Doctor profile and catalog")
// under development
public class DoctorController {

    private final DoctorService doctorService;

    @PutMapping("/me/education")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> replaceEducation(
            @Valid @RequestBody List<@Valid EducationItemRequest> education) {
        DoctorProfileResponse response = doctorService.replaceEducation(education);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/experience")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> replaceExperience(
            @Valid @RequestBody List<@Valid ExperienceItemRequest> experience) {
        DoctorProfileResponse response = doctorService.replaceExperience(experience);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/price")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> setPrice(
            @Valid @RequestBody SetDoctorPriceRequest request) {
        DoctorProfileResponse response = doctorService.setPrice(request.price());
        return ResponseEntity.ok(response);
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
