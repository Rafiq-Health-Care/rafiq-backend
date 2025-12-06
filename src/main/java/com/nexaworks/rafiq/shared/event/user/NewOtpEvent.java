package com.nexaworks.rafiq.shared.event.user;

public record NewOtpEvent(String email, String otp, String name) {
}
