package com.nexaworks.rafiq.dto.event;


public record UserRegistrationEvent(String email, String otp,String name) {
}
