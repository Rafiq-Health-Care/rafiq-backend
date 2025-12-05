package com.nexaworks.rafiq.user.event;

public record ForgetPasswordEvent(String email, String accessToken, String name) {
}
