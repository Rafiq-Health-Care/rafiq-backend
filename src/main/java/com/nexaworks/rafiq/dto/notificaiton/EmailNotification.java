package com.nexaworks.rafiq.dto.notificaiton;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record EmailNotification(@Email @NotBlank String email,
                                @NotBlank String template,
                                @NotBlank String subject,
                                Map<String,Object > variables)  implements Notification{
}
