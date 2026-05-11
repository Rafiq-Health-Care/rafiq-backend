package com.nexaworks.rafiq.dto.request.doctor;

import java.math.BigDecimal;
import java.util.List;

import com.nexaworks.rafiq.entities.enums.Availability;
import com.nexaworks.rafiq.entities.enums.Gender;

public record DoctorFilter(List<String> specialities, Availability availability, double rating,
        BigDecimal minPrice, BigDecimal maxPrice, Gender gender) {
}
