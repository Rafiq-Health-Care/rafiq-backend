package com.nexaworks.rafiq.dto.request.basicMedicalProfile;

import java.util.Date;

import com.nexaworks.rafiq.entities.enums.BloodType;
import com.nexaworks.rafiq.entities.enums.SmokeStatus;

import jakarta.validation.constraints.Positive;

public record CreateBasicMedicalProfileRequest(@Positive Integer heightInCm,
        @Positive Double weightInKg, BloodType bloodType, SmokeStatus smokeStatus,
        Integer cigarettesPerDay, Date lastSmoked, boolean alcoholism, int drinksPerWeek,
        boolean pregnant, String occupation, String emergencyContactName,
        String emergencyContactPhone) {
}
