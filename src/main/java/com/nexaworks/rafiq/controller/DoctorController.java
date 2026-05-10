package com.nexaworks.rafiq.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.nexaworks.rafiq.dto.request.doctor.EducationItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.ExperienceItemRequest;
import com.nexaworks.rafiq.dto.request.doctor.SetDoctorPriceRequest;
import com.nexaworks.rafiq.dto.response.doctor.DoctorProfileResponse;
import com.nexaworks.rafiq.entities.Doctor;
import com.nexaworks.rafiq.mapper.DoctorMapper;
import com.nexaworks.rafiq.service.doctor.DoctorService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor", description = "Doctor profile and catalog")
public class DoctorController {

    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;

    @PutMapping("/me/education")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> replaceEducation(
            @Valid @RequestBody List<@Valid EducationItemRequest> education) {
        Doctor doctor = doctorService.replaceEducation(education);
        return ResponseEntity.ok(doctorMapper.toProfileResponse(doctor));
    }

    @PutMapping("/me/experience")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> replaceExperience(
            @Valid @RequestBody List<@Valid ExperienceItemRequest> experience) {
        Doctor doctor = doctorService.replaceExperience(experience);
        return ResponseEntity.ok(doctorMapper.toProfileResponse(doctor));
    }

    @PutMapping("/me/price")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorProfileResponse> setPrice(
            @Valid @RequestBody SetDoctorPriceRequest request) {
        Doctor doctor = doctorService.setPrice(request.price());
        return ResponseEntity.ok(doctorMapper.toProfileResponse(doctor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorProfileResponse> getDoctorById(@PathVariable UUID id) {
        Doctor doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctorMapper.toProfileResponse(doctor));
    }

}
