package com.nexaworks.rafiq.dto.request.doctor;

import java.util.Set;

import com.nexaworks.rafiq.entities.enums.Language;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.entities.enums.SubSpecialization;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateBasicInfoRequest(@NotBlank String firstName, @NotBlank String lastName,
        @NotNull Specialization specialization, @NotNull Set<SubSpecialization> subSpecializations,
        @NotNull Set<Language> languages, @NotBlank String description) {
}
