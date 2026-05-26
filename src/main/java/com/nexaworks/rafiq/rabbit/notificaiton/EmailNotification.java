package com.nexaworks.rafiq.rabbit.notificaiton;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailNotification(UUID notificationId, @Email @NotBlank String email,
        @NotBlank String template, @NotBlank String subject,
        Map<String, Object> variables) implements Notification {

    public EmailNotification {
        notificationId = notificationId != null ? notificationId : UUID.randomUUID();
    }

    public static EmailNotification of(String email, String template, String subject,
            Map<String, Object> variables) {
        return new EmailNotification(UUID.randomUUID(), email, template, subject, variables);
    }
}
