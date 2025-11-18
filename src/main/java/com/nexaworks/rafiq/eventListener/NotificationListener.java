package com.nexaworks.rafiq.eventListener;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.nexaworks.rafiq.dto.event.ForgetPasswordEvent;
import com.nexaworks.rafiq.dto.event.NewOtpEvent;
import com.nexaworks.rafiq.dto.event.UserRegistrationEvent;
import com.nexaworks.rafiq.service.EmailContentService;
import com.nexaworks.rafiq.service.EmailSenderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {
    private final EmailContentService emailContentService;
    private final EmailSenderService emailSenderService;

    @Async
    @EventListener
    public void handleUserRegistrationEvent(UserRegistrationEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(),
                "url");
        log.info("Sending email to {}", event.email());
        emailSenderService.sendEmail(model, event.email(), "Verify your email address",
                "OTP_TEMPLATE.html");
    }

    @Async
    @EventListener
    public void handleNewOtpEvent(NewOtpEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(),
                "url");
        log.info("Sending email to {}", event.email());
        emailSenderService.sendEmail(model, event.email(), "New OTP", "new-otp.html");
    }

    @Async
    @EventListener
    public void handleForgetPasswordEvent(ForgetPasswordEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(),
                "url");
        log.info("Sending email to {}", event.email());
        emailSenderService.sendEmail(model, event.email(), "Reset your password",
                "forget-password.html");
    }
}
