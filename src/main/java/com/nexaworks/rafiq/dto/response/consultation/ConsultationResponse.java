package com.nexaworks.rafiq.dto.response.consultation;

import com.nexaworks.rafiq.dto.response.doctor.DoctorDto;
import com.nexaworks.rafiq.dto.response.patientProfile.PatientDto;
import com.nexaworks.rafiq.entities.enums.ConsultationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ConsultationResponse(UUID id, LocalTime startTime, int duration, LocalDateTime date,
                                   ConsultationStatus status, BigDecimal price, PatientDto patient, DoctorDto doctor) {
}
