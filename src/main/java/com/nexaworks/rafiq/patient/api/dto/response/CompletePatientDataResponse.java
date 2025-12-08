package com.nexaworks.rafiq.patient.api.dto.response;

import java.util.UUID;

import com.nexaworks.rafiq.patient.entity.enums.BloodType;
import com.nexaworks.rafiq.patient.entity.enums.SmokeStatus;
import com.nexaworks.rafiq.user.entity.enums.Gender;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Complete patient data response")
public record CompletePatientDataResponse(String firstName, String lastName, Gender gender,
        String phoneNumber, String email, int height, double weight, BloodType bloodType,
        SmokeStatus smokeStatus, boolean alcoholism, String occupation, String emergencyContactName,
        String emergencyContactPhone, String description, String bmi, UUID patientId) {
}
