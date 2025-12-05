package com.nexaworks.rafiq.dto.response.patientProfile;

import java.util.UUID;

import com.nexaworks.rafiq.entities.enums.BloodType;
import com.nexaworks.rafiq.entities.enums.SmokeStatus;
import com.nexaworks.rafiq.user.entity.enums.Gender;

public record PatientProfileResponse(String firstName, String lastName, String age, Gender gender,
        String phoneNumber, String email, int height, double weight, BloodType bloodType,
        SmokeStatus smokeStatus, boolean alcoholism, String occupation, String emergencyContactName,
        String emergencyContactPhone, String description, String bmi, UUID patientId) {
}
