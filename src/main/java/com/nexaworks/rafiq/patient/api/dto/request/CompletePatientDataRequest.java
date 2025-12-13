package com.nexaworks.rafiq.patient.api.dto.request;

import java.util.Date;

import com.nexaworks.rafiq.patient.entity.enums.BloodType;
import com.nexaworks.rafiq.patient.entity.enums.SmokeStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

@Schema(description = "Complete patient data request")
public record CompletePatientDataRequest(@Positive @Schema Integer heightInCm,
        @Positive @Schema Double weightInKg, @Schema BloodType bloodType,
        @Schema SmokeStatus smokeStatus, @Schema Integer cigarettesPerDay, @Schema Date lastSmoked,
        @Schema boolean alcoholism, @Schema int drinksPerWeek, @Schema boolean pregnant,
        @Schema String occupation, @Schema String emergencyContactName,
        @Schema String emergencyContactPhone) {
}
