package com.nexaworks.rafiq.user.event;

public record NewOtpEvent(String email, String otp, String name) {
}
