package com.nexaworks.rafiq.dto.response.doctor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DoctorSearchResponse(String personalPhoto, String firstName, String lastName,
        String specialization, LocalDateTime nextAvailable, BigDecimal price, BigDecimal rating,
        int yearsOfExperience, UUID doctorId) {
}
