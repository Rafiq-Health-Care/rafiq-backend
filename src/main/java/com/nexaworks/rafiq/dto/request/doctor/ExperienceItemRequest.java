package com.nexaworks.rafiq.dto.request.doctor;

import com.nexaworks.rafiq.validation.annotation.ValidExperienceItem;
import com.nexaworks.rafiq.validation.annotation.YearNotAfterCurrent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ValidExperienceItem
public record ExperienceItemRequest(@NotBlank String position, @NotBlank String hospital,
        @NotNull @YearNotAfterCurrent Integer startYear, Integer endYear, String description) {
}
