package com.nexaworks.rafiq.shared.event.user;

public record ForgetPasswordEvent(String email, String accessToken, String name) {
}
