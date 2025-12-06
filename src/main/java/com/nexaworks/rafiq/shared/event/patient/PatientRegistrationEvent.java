package com.nexaworks.rafiq.shared.event.patient;

public record PatientRegistrationEvent(String email, String otp, String name) {
}
