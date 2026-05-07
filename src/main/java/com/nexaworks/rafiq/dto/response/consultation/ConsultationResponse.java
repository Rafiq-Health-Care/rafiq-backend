package com.nexaworks.rafiq.dto.response.consultation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.nexaworks.rafiq.dto.response.doctor.DoctorDto;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientDto;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

public record ConsultationResponse(UUID id, LocalDateTime startTime, int duration,
        ConsultationStatus status, BigDecimal price, PatientDto patient, DoctorDto doctor,
        LocalDateTime bookedAt, LocalDateTime cancelledAt, String reason, String notes,
        boolean cancelByPatient) {
}
