package com.nexaworks.rafiq.rabbit.notificaiton;

import java.util.Map;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailNotification(@Email @NotBlank String email, @NotBlank String template,
        @NotBlank String subject, Map<String, Object> variables) implements Notification {
}
