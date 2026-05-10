package com.nexaworks.rafiq.dto.request.doctor;

import com.nexaworks.rafiq.validation.annotation.YearNotAfterCurrent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EducationItemRequest(@NotBlank String degree, @NotBlank String university,
        @NotNull @YearNotAfterCurrent Integer startYear, Integer endYear) {
}
