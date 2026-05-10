package com.nexaworks.rafiq.dto.response.doctor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.Specialization;

public record DoctorProfileResponse(UUID id, String name, String personalPhoto, String biography,
        String description, BigDecimal price, Specialization specialization,
        List<EducationItemResponse> education, List<ExperienceItemResponse> experience) {
}
