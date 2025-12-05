package com.nexaworks.rafiq.user.event;

public record PatientRegistrationEvent(String email, String otp, String name) {
}
