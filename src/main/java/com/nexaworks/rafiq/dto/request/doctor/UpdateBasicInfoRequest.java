package com.nexaworks.rafiq.dto.request.doctor;

import java.util.Set;

import com.nexaworks.rafiq.entities.enums.Language;
import com.nexaworks.rafiq.entities.enums.Specialization;
import com.nexaworks.rafiq.entities.enums.SubSpecialization;

import jakarta.validation.constraints.NotBlank;

public record UpdateBasicInfoRequest(@NotBlank String firstName, @NotBlank String lastName,
        @NotBlank Specialization specialization,
        @NotBlank Set<SubSpecialization> subSpecializations, @NotBlank Set<Language> languages,
        @NotBlank String description) {
}
