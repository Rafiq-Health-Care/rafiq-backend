package com.nexaworks.rafiq.dto.event;

public record ForgetPasswordEvent(String email, String accessToken, String name) {
}
