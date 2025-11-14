package com.nexaworks.rafiq.eventListener;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistrationEvent(UserRegistrationEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(), "url");
        log.info("Sending email to {}", event.email());
        emailSenderService.sendEmail(model, event.email(), "Verify your email address", "OTP_TEMPLATE.html");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNewOtpEvent(NewOtpEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(), "url");
        log.info("Sending email to {}", event.email());
        emailSenderService.sendEmail(model, event.email(), "New OTP", "new-otp.html");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleForgetPasswordEvent(ForgetPasswordEvent event) {
        Map<String, Object> model = emailContentService.createOtpEmail(event.otp(), event.name(), "url");
        log.info("Sending email to {}", event.email());
        emailSenderService.sendEmail(model, event.email(), "Reset your password", "forget-password.html");
    }
}
