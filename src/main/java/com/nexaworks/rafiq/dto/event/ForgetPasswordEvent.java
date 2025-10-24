package com.nexaworks.rafiq.dto.event;

public record ForgetPasswordEvent(String email,String otp,String name) {
}
