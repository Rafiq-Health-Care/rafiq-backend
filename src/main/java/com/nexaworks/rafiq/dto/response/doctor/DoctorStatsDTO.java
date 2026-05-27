package com.nexaworks.rafiq.dto.response.doctor;

import java.time.LocalDateTime;

public record DoctorStatsDTO(LocalDateTime startTime, Long consultationsCount) {
}
