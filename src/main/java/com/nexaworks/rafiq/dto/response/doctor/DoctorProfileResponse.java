package com.nexaworks.rafiq.dto.response.doctor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.nexaworks.rafiq.entities.Education;
import com.nexaworks.rafiq.entities.Experience;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.entities.enums.SubSpecialization;

public record DoctorProfileResponse(UUID id, String firstName, String lastName,
        String personalPhoto, String biography,

        String description, BigDecimal price, Specialization specialization,
        Set<SubSpecialization> subSpecializations, List<Education> education,
        List<Experience> experience, LocalDateTime nextAvailable, Long consultationCount,
        BigDecimal rating, int yearsOfExperience) {
}
