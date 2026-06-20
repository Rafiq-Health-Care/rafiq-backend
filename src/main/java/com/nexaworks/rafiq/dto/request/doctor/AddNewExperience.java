package com.nexaworks.rafiq.dto.request.doctor;

import java.time.LocalDate;

public record AddNewExperience(String position, String hospitalName, LocalDate startDate,
        LocalDate endDate, String description, boolean currentJob) {
}
