package com.nexaworks.rafiq.shared.event.patient;

import java.util.UUID;

public record PatientRegistrationEvent(String email, String otp, String firstName, String lastName,
        UUID userId) {
}
