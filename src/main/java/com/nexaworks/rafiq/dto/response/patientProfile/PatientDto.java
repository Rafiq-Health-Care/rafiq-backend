package com.nexaworks.rafiq.dto.response.patientProfile;

import java.util.UUID;

public record PatientDto(UUID id, String firstName, String lastName) {
}
